package org.example.sansam.notification.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.sansam.notification.domain.Notification;
import org.example.sansam.notification.domain.NotificationHistories;
import org.example.sansam.notification.dto.NotificationDTO;
import org.example.sansam.notification.event.ChatEvent;
import org.example.sansam.notification.exception.CustomException;
import org.example.sansam.notification.exception.ErrorCode;
import org.example.sansam.notification.repository.NotificationHistoriesRepository;
import org.example.sansam.notification.repository.NotificationsRepository;
import org.example.sansam.notification.template.NotificationType;
import org.example.sansam.user.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.core.task.TaskTimeoutException;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    private final NotificationsRepository notificationRepository;
    private final NotificationHistoriesRepository notificationHistoriesRepository;
    private final Map<Long, SseEmitter> sseEmitters = new ConcurrentHashMap<>();
    private static final long DEFAULT_TIMEOUT = 60L * 1000 * 30; // 30분 설정, 리소스 점유 시간 절감
    @Autowired
    private ObjectMapper objectMapper;

    // public 메서드

    public SseEmitter connect(Long userId) {

        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("Invalid userId: " + userId);
        }

        // 동시성 제어
        synchronized (sseEmitters) {
            SseEmitter existingEmitter = sseEmitters.get(userId);
            if (existingEmitter != null) {
                log.info("기존 SSE 연결 제거 - userId: {}", userId);
                existingEmitter.complete(); // 이전 연결 정리
                sseEmitters.remove(userId);
            }

            SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
            sseEmitters.put(userId, emitter);
            log.info("새 SSE 연결 생성 - userId: {}, 연결 수: {}", userId, sseEmitters.size());

            // 연결 종료 시 emitter 제거
            emitter.onCompletion(() -> {
                log.info("SSE 연결 종료 - userId: {}", userId);
                sseEmitters.remove(userId);
            });

            emitter.onTimeout(() -> {
                log.warn("SSE 연결 타임아웃 - userId: {}", userId);
                sseEmitters.remove(userId);
            });

            emitter.onError(ex -> {
                log.error("SSE 연결 에러 - userId: {}, error: {}", userId, ex.getMessage());
                sseEmitters.remove(userId);
            });

            return emitter;
        }
    }

    public List<NotificationDTO> getNotificationHistories(Long userId) {
        return notificationHistoriesRepository.findAllByUser_Id(userId)
                .stream().map(NotificationDTO::from).toList();
    }

    public Long getUnreadNotificationCount(Long userId) {
        return notificationHistoriesRepository.countByUser_IdAndIsReadFalse(userId);
    }

    @Transactional
    public void markAsRead(Long notificationHistoriesId) {
        NotificationHistories notification = notificationHistoriesRepository.findById(notificationHistoriesId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOTIFICATION_HISTORY_ID_NOT_FOUND));

        if (!notification.isRead()) {
            notification.setRead(true);
        }
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        List<NotificationHistories> unreadNotifications =
                notificationHistoriesRepository.findAllByUser_IdAndIsReadFalse(userId);

        unreadNotifications.forEach(n -> n.setRead(true));
    }

    public void deleteNotificationHistory(Long userId, Long notificationHistoriesId) {
        notificationHistoriesRepository.deleteByUser_IdAndId(userId, notificationHistoriesId);
    }

    public void sendWelcomeNotification(User user) {
        sendNotification(user, NotificationType.WELCOME, user.getName(), "");
    }

    public void sendPaymentCompleteNotification(User user, String orderName, Long orderPrice) {
        String messageParam = user.getName() + ","  + orderName + "," + orderPrice;
        sendNotification(user, NotificationType.PAYMENT_COMPLETE, "", messageParam);
    }

    public void sendPaymentCancelNotification(User user, String orderName, Long refundPrice) {
        String messageParam = user.getName()+ "," + orderName + "," + refundPrice;
        sendNotification(user, NotificationType.PAYMENT_CANCEL, "", messageParam);
    }

    public void sendCartLowNotification(User user, String productName) {
        sendNotification(user, NotificationType.CART_LOW, "", productName);
    }

    public void sendWishListLowNotification(User user, String productName) {
        sendNotification(user, NotificationType.WISH_LOW, "", productName);
    }

    public void sendReviewRequestNotification(User user, String orderName) {
        String messageParam = user.getName() + "," + orderName;
        sendNotification(user, NotificationType.REVIEW_REQUEST, "", messageParam);
    }

    public void sendChatNotification(User user, String chatRoomName, String message) {
        sendNotification(user, NotificationType.CHAT, chatRoomName, message);
    }

    // private 메서드

    private void sendNotification(User user, NotificationType type, String titleParam, String messageParam) {
        Notification template = getTemplateOrThrow(type.getTemplateId());
        String title = formatTitle(template.getTitle(), titleParam);
        String message = formatMessage(template.getMessage(), messageParam);

        NotificationHistories saved = saveNotificationHistory(user, template, title, message);
        String payload = serializeToJson(NotificationDTO.from(saved));
        log.info(payload);
        sendViaSSEAsync(user.getId(), payload, type.getEventName());

        log.info("알림 전송 완료 - 사용자: {}, 이벤트: {}", user.getName(), type.getEventName());
    }

    private Notification getTemplateOrThrow(Long templateId) {
        return notificationRepository.findById(templateId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOTIFICATION_TEMPLATE_NOT_FOUND));
    }

    private String formatTitle(String template, String param) {
        try {
            return String.format(template, param);
        } catch (IllegalFormatException e) {
            throw new CustomException(ErrorCode.NOTIFICATION_TEMPLATE_FORMAT_ERROR);
        }
    }

    private String formatMessage(String template, String param) {
        try {
            int placeholderCount = countPlaceholders(template);
            String[] parts = param.split(",", -1); // 빈 문자열도 유지

            if (parts.length < placeholderCount) {
                throw new CustomException(ErrorCode.NOTIFICATION_TEMPLATE_FORMAT_ERROR);
            }

            // 파라미터 수에 맞춰 잘라내기
            Object[] args = new Object[placeholderCount];
            System.arraycopy(parts, 0, args, 0, placeholderCount);

            return String.format(template, args);
        } catch (IllegalFormatException e) {
            throw new CustomException(ErrorCode.NOTIFICATION_TEMPLATE_FORMAT_ERROR);
        }
    }

    private int countPlaceholders(String template) {
        int count = 0;
        int idx = 0;

        while ((idx = template.indexOf("%s", idx)) != -1) {
            count++;
            idx += 2;
        }
        return count;
    }

    private NotificationHistories saveNotificationHistory(User user, Notification template, String title, String message) {
        NotificationHistories notification = NotificationHistories.builder()
                .user(user)
                .notification(template)
                .title(title)
                .message(message)
                .createdAt(Timestamp.valueOf(LocalDateTime.now()))
                .expiredAt(Timestamp.valueOf(LocalDateTime.now().plusDays(14)))
                .isRead(false)
                .build();

        NotificationHistories saved = notificationHistoriesRepository.save(notification);
        if (saved.getId() == null) {
            throw new CustomException(ErrorCode.NOTIFICATION_SAVE_FAILED);
        }
        return saved;
    }

    private String serializeToJson(NotificationDTO dto) {
        try {
            return objectMapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorCode.NOTIFICATION_SERIALIZATION_FAILED);
        }
    }

    @Async
    @Retryable(
            include = {TaskRejectedException.class, IOException.class},    // 이 리스트에 명시된 예외가 발생했을 때만 재시도
            maxAttempts = 3,    // 최대 시도 횟수 (최초 실행 포함) 실패 시 예외 그대로 던짐
            backoff = @Backoff(delay = 1000, multiplier = 2)    // 대기시간 1000, 2000, 4000ms
    )
    protected void sendViaSSEAsync(Long userId, String payload, String eventName) {
        try {
            SseEmitter emitter = sseEmitters.get(userId);
            log.info("Emitter 상태 확인 - userId: {}, emitter: {}", userId, emitter == null ? "null" : "존재");

            if (emitter == null) {
                log.warn("Emitter가 없음 - userId: {}, 알림은 저장됨", userId);
                // SSE 전송 실패해도 알림은 이미 저장되었으므로 예외를 던지지 않음
                return;
            }

            log.info("payload: {}, eventName: {}", payload, eventName);
            emitter.send(SseEmitter.event()
                    .name(eventName)
                    .data(payload, MediaType.APPLICATION_JSON));
            log.info("SSE 전송 완료: userId: {}, eventName: {}", userId, eventName);

        }catch (IOException e) {
            log.error("SSE 전송 실패 - userId: {}, event: {}, error: {}", userId, eventName, e.getMessage() );

            SseEmitter emitter = sseEmitters.get(userId);
            if (emitter != null) {
                emitter.completeWithError(e);
                sseEmitters.remove(userId);
                log.info("SSE emitter 제거 - userId: {}", userId);
            }
        }

    }
}