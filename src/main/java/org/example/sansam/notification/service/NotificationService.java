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
    private final ApplicationEventPublisher publisher;
//    private final Map<Long, SseEmitter> sseEmitters = new ConcurrentHashMap<>();
//    private static final long DEFAULT_TIMEOUT = 60L * 1000 * 30; // 30분 설정, 리소스 점유 시간 절감
    private final ObjectMapper objectMapper;

    // public 메서드

    public SseEmitter connect(Long userId) {

        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("Invalid userId: " + userId);
        }

        SseEmitter existingEmitter = sseEmitters.get(userId);
        if (existingEmitter != null) {
            existingEmitter.complete(); // 이전 연결 정리
            sseEmitters.remove(userId);
        }

        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
        sseEmitters.put(userId, emitter);

        // 연결 종료 시 emitter 제거
        emitter.onCompletion(() -> sseEmitters.remove(userId));
        emitter.onTimeout(() -> sseEmitters.remove(userId));
        emitter.onError(ex -> sseEmitters.remove(userId));

        return emitter;

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
        notificationHistoriesRepository.findAllByUser_IdAndIsReadFalse(userId);
    }

    // TODO: AOP 학습 -> Spring의 트랜잭션은 기본적으로 프록시로 적용됨
    //  프록시를 통해 진입하는 외부 호출만 가로챌 수 있음
    //  스프링 레퍼런스 기준으로 proxy 모드에서는 public 메서드만 어드바이스 적용을 보장합니다. -> 접근 불가하다!
    private void sendNotification(User user, NotificationType type, String titleParam, String messageParam) {
        Notification template = getTemplateOrThrow(type.getTemplateId());
        String title = formatTitle(template.getTitle(), titleParam);
        String message = formatMessage(template.getMessage(), messageParam);

        NotificationHistories saved = saveNotificationHistory(user, template, title, message);


        String payload = serializeToJson(NotificationDTO.from(saved));
//        sendViaSSEAsync(user.getId(), payload, type.getEventName()); //TODO: 트랜잭션이랑 별개의 작업인데 트랜잭션안에서 있다????
        // think : DB를 건드리지 않는 작업이기 때문에 따로 event와 listener로 빼서
        //  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
        //  지금 여기 속해 있는 트랜잭션에 대해서 commit이 된 후에 event 발행!

        publisher.publishEvent(NotificationSavedEvent.of(user.getId(), type.getEventName(), payload));
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
                .createdAt(LocalDateTime.now())
                .expiredAt(LocalDateTime.now().plusDays(14))
                .isRead(false)
                .build();

//        NotificationHistories saved = notificationHistoriesRepository.save(notification);
        //        if (saved.getId() == null) { //fix:이부분 확인 필요
//            throw new CustomException(ErrorCode.NOTIFICATION_SAVE_FAILED);
//        }
        // think: 성공/실패 기준으로 부적절함.
        //  save() 호출 뒤에 플러시나 커밋 시점이 생길 수도 있기 때문에 오류가 발생
        //  (내가 생각한 부분은 saved.getId() == null)이 반영이 안 될 가능성도 있기 때문에 굳이?인 코드였다.
        //  그리고 실패는 예외로 구분하는 것이 맞기 때문에 불필요한 분기/중복이다.
        //  저장 성공 여부는 영속성 계층의 책임.
        return notificationHistoriesRepository.save(notification);
    }

    //TODO: 공통화 작업 필요
    private String serializeToJson(NotificationDTO dto) {
        try {
            return objectMapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorCode.NOTIFICATION_SERIALIZATION_FAILED);
        }
    }

    @Transactional(readOnly = true)
    public List<NotificationDTO> getNotificationHistories(Long userId) {
        return notificationHistoriesRepository.findAllByUser_Id(userId)
                .stream().map(NotificationDTO::from).toList();
    }

    @Transactional(readOnly = true)
    public Long getUnreadNotificationCount(Long userId) {
        return notificationHistoriesRepository.countByUser_IdAndIsReadFalse(userId);
    }

    @Transactional
    public void markAsRead(Long notificationHistoriesId) {
        notificationHistoriesRepository.findByIsReadFalse(notificationHistoriesId);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        notificationHistoriesRepository.findAllByUser_IdAndIsReadFalse(userId);
    }

    @Transactional
    public void deleteNotificationHistory(Long userId, Long notificationHistoriesId) {
        notificationHistoriesRepository.deleteByUser_IdAndId(userId, notificationHistoriesId);
    }


    @Transactional
    public void sendWelcomeNotification(User user) {
        sendNotification(user, NotificationType.WELCOME, user.getName(), "");
    }
    @Transactional
    public void sendPaymentCompleteNotification(User user, String orderName, Long orderPrice) {
        String messageParam = user.getName() + "," + orderName + "," + orderPrice;
        sendNotification(user, NotificationType.PAYMENT_COMPLETE, "", messageParam);
    }
    @Transactional
    public void sendPaymentCancelNotification(User user, String orderName, Long refundPrice) {
        String messageParam = user.getName() + "," + orderName + "," + refundPrice;
        sendNotification(user, NotificationType.PAYMENT_CANCEL, "", messageParam);
    }
    @Transactional
    public void sendCartLowNotification(User user, String productName) {
        sendNotification(user, NotificationType.CART_LOW, "", productName);
    }
    @Transactional
    public void sendWishListLowNotification(User user, String productName) {
        sendNotification(user, NotificationType.WISH_LOW, "", productName);
    }
    @Transactional
    public void sendReviewRequestNotification(User user, String orderName) {
        String messageParam = user.getName() + "," + orderName;
        sendNotification(user, NotificationType.REVIEW_REQUEST, "", messageParam);
    }
    @Transactional
    public void sendChatNotification(User user, String chatRoomName, String message) {
        sendNotification(user, NotificationType.CHAT, chatRoomName, message);
    }

}