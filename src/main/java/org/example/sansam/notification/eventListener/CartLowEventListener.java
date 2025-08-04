package org.example.sansam.notification.eventListener;

import lombok.AllArgsConstructor;
import org.example.sansam.notification.event.CartLowEvent;
import org.example.sansam.notification.service.NotificationService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class CartLowEventListener {

    private final NotificationService notificationService;

    @EventListener
    public void handleCartLowEvent(CartLowEvent event){
        notificationService.sendCartLowNotification(event.getUser(), event.getProductName());
    }
}
