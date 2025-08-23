package org.example.sansam.notification.eventListener.sse;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.sansam.notification.event.sse.ReviewRequestEvent;
import org.example.sansam.notification.service.NotificationService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Log4j2
public class ReviewRequestEventListener {
    private final NotificationService notificationService;

    @EventListener
    public void handleReviewRequestEvent(ReviewRequestEvent event) {
        try {
            notificationService.sendReviewRequestNotification(event.getUser(), event.getOrderName());
        } catch (Exception e) {
            log.error("리뷰 요청 알림 실패 - userId={}", event.getUser() != null ? event.getUser().getId() : "null", e);
        }
    }
}
