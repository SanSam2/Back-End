package org.example.sansam.notification.eventListener;

import lombok.AllArgsConstructor;
import org.example.sansam.notification.event.ReviewRequestEvent;
import org.example.sansam.notification.service.NotificationService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@AllArgsConstructor
public class ReviewRequestEventListener {
    private final NotificationService notificationService;

    @EventListener
    public void handleReviewRequestEvent(ReviewRequestEvent event) throws IOException {
        notificationService.sendReviewRequestNotification(event.getUser(), event.getOrderName());
    }
}
