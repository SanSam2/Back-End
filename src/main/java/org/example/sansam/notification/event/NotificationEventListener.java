package org.example.sansam.notification.event;

import lombok.RequiredArgsConstructor;
import org.example.sansam.notification.service.NotificationService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationService notificationService;

    @EventListener
    public void handelNotificationEvent(NotificationEvent event) {
        Long userId = event.getUserId();
        Object payload = event.getPayload();

        notificationService.sendDynamicNotification(userId, payload);
    }
}
