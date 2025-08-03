package org.example.sansam.notification.eventListener;

import lombok.AllArgsConstructor;
import org.example.sansam.notification.event.CartLowEvent;
import org.example.sansam.notification.service.NotificationService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@AllArgsConstructor
public class CartLowEventListener {

    private final NotificationService notificationService;

    @Async
    @EventListener
    public void handleCartLowEvent(CartLowEvent event) throws IOException {
        notificationService.sendCartLowNotification(event.getUser(), event.getProductName());
    }
}
