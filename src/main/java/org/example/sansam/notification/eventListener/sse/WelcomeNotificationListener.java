package org.example.sansam.notification.eventListener.sse;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.sansam.notification.event.sse.WelcomeNotificationEvent;
import org.example.sansam.notification.service.NotificationService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Log4j2
public class WelcomeNotificationListener {
    private NotificationService notificationService;

    @EventListener
    public void handelWelcomeNotificationEvent(WelcomeNotificationEvent event) {
        try {
            notificationService.sendWelcomeNotification(event.getUser());
        } catch (Exception e) {
            log.error("회원가입 환영 알림 실패 - userId={}", event.getUser() != null ? event.getUser().getId() : "null", e);
        }
    }
}
