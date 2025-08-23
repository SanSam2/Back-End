package org.example.sansam.notification.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.sansam.notification.domain.Notification;
import org.example.sansam.notification.domain.NotificationHistories;
import org.example.sansam.notification.dto.NotificationDTO;
import org.example.sansam.notification.event.sse.NotificationSavedEvent;
import org.example.sansam.notification.exception.CustomException;
import org.example.sansam.notification.exception.ErrorCode;
import org.example.sansam.notification.repository.NotificationHistoriesRepository;
import org.example.sansam.notification.repository.NotificationsRepository;
import org.example.sansam.notification.domain.NotificationType;
import org.example.sansam.user.domain.User;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.IllegalFormatException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    private final NotificationsRepository notificationRepository;
    private final NotificationHistoriesRepository notificationHistoriesRepository;
    private final ApplicationEventPublisher publisher;
    private final ObjectMapper objectMapper;

    // public 메서드
    private void sendNotification(User user, NotificationType type, String titleParam, String messageParam) {
        Notification template = getTemplateOrThrow(type.getTemplateId());

        String title = formatTitle(template.getTitle(), titleParam);
        String message = formatMessage(template.getMessage(), messageParam);

        NotificationHistories saved = saveNotificationHistory(user, template, title, message);
        String payload = serializeToJson(NotificationDTO.from(saved));

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