package org.example.sansam.notification.eventListener;

import lombok.AllArgsConstructor;
import org.example.sansam.notification.event.ChatEvent;
import org.example.sansam.notification.service.NotificationService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@AllArgsConstructor
public class ChatEventListener {
    private final NotificationService notificationService;

    @EventListener
    public void handleChatEvent(ChatEvent event) {
        notificationService.sendChatNotification(event.getUser(), event.getSenderName(), event.getMessage());
    }
}
