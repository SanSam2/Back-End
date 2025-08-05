package org.example.sansam.notification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.sansam.notification.domain.Notification;
import org.example.sansam.notification.domain.NotificationHistories;
import org.example.sansam.notification.dto.NotificationDTO;
import org.example.sansam.notification.exception.CustomException;
import org.example.sansam.notification.exception.ErrorCode;
import org.example.sansam.notification.repository.NotificationHistoriesRepository;
import org.example.sansam.notification.repository.NotificationsRepository;
import org.example.sansam.user.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
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
    @Autowired
    private ObjectMapper objectMapper;

    private static final long DEFAULT_TIMEOUT = 60L * 1000 * 60;

    // === Public Methods (외부 호출용 API) ===

    public SseEmitter connect(Long userId) {
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
        sseEmitters.put(userId, emitter);

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
    public void markAsRead(Long notificationId) {
        NotificationHistories notification = notificationHistoriesRepository.findById(notificationId)
                .orElseThrow(() -> new CustomException(ErrorCode.EMITTER_NOT_FOUND));

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

    public void sendNotification(User user, Long templateId, String titleParam, String messageParam, String eventName) throws IOException {
        Notification template = notificationRepository.findById(templateId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOTIFICATION_TEMPLATE_NOT_FOUND));

        String title = String.format(template.getTitle(), titleParam);
        String message = String.format(template.getMessage(), messageParam);

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

        NotificationDTO dto = NotificationDTO.from(saved);

        String payload = objectMapper.writeValueAsString(dto);

        if (sseEmitters.containsKey(user.getId())) {
            sseEmitters.get(user.getId()).send(SseEmitter.event()
                    .name(eventName)
                    .data(payload, MediaType.APPLICATION_JSON));
        }

        log.info("알림 전송 완료 - 사용자: {}, 이벤트: {}", user.getName(), eventName);
    }

    public void sendWelcomeNotification(User user) throws IOException {
        sendNotification(user, 1L, user.getName(), "", "welcomeMessage");
    }

    public void sendPaymentCompleteNotification(User user, String orderName, Long orderPrice) throws IOException {
        sendNotification(user, 2L, user.getName(), orderName + "," + orderPrice, "paymentComplete");
    }

    public void sendPaymentCancelNotification(User user, String orderName, Long refundPrice) throws IOException {
        sendNotification(user, 3L, user.getName(), orderName + "," + refundPrice, "paymentCancel");
    }

    public void sendCartLowNotification(User user, String productName) throws IOException {
        sendNotification(user, 4L, productName, productName, "cartProductStockLowMessage");
    }

    public void sendWishListLowNotification(User user, String productName) throws IOException {
        sendNotification(user, 5L, productName, productName, "wishListProductStockLow");
    }

    public void sendReviewRequestNotification(User user, String orderName) throws IOException {
        sendNotification(user, 6L, user.getName(), orderName, "reviewRequestMessage");
    }

    public void sendChatNotification(User user, Long senderId, String senderName) throws IOException {
        sendNotification(user, 7L, "", senderName, "chatNotificationMessage");
    }

}