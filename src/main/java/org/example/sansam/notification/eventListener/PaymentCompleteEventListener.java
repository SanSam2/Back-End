package org.example.sansam.notification.eventListener;

import lombok.AllArgsConstructor;
import org.example.sansam.notification.event.PaymentCompleteEvent;
import org.example.sansam.notification.service.NotificationService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class PaymentCompleteEventListener {

    private final NotificationService notificationService;

    @EventListener
    public void handelPaymentCompleteEvent(PaymentCompleteEvent event) {
        notificationService.sendPaymentCompleteNotification(event.getUser(), event.getOrderName(), event.getOrderPrice());
    }
}
