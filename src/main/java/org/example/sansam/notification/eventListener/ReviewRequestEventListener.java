package org.example.sansam.notification.eventListener;

import lombok.AllArgsConstructor;
import org.example.sansam.notification.event.ReviewRequestEvent;
import org.example.sansam.notification.service.NotificationService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ReviewRequestEventListener {

    private final NotificationService notificationService;

    @EventListener
    public void handleReviewRequestEvent(ReviewRequestEvent event){
        notificationService.sendReviewRequestNotification(event.getUserId(), event.getUsername(), event.getProductName());
    }

}
