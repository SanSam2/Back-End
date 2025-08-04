package org.example.sansam.notification.eventListener;

import jdk.jfr.Event;
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
    public void handReviewRequestEvent(ReviewRequestEvent event){
        notificationService.sendReviewRequestNotification(event.getUser(), event.getProductName());
    }
}
