package org.example.sansam.notification.eventListener;

import lombok.AllArgsConstructor;
import org.example.sansam.notification.event.WelcomeNotificationEvent;
import org.example.sansam.notification.service.NotificationService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class WelcomeNotificationListener {
    private NotificationService notificationService;

    @EventListener
    public void handelWelcomeNotificationEvent(WelcomeNotificationEvent event) {
        notificationService.sendWelcomeNotification(event.getUser());
    }
}
