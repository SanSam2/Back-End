package org.example.sansam.notification.eventListener;

import lombok.AllArgsConstructor;
import org.example.sansam.notification.event.WishListLowEvent;
import org.example.sansam.notification.service.NotificationService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class WishListLowEventListener {

    private final NotificationService notificationService;

    @EventListener
    public void handelWishListLowEvent(WishListLowEvent event){
        notificationService.sendWishListLowNotification(event.getUser(), event.getProductName());
    }
}
