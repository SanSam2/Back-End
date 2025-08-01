package org.example.sansam.notification.eventListener;

import lombok.AllArgsConstructor;
import org.example.sansam.notification.event.PaymentCancelEvent;
import org.example.sansam.notification.service.NotificationService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class PaymentCancelEventListener {

    private final NotificationService notificationService;

    @EventListener
    public void handelPaymentCancelEvent(PaymentCancelEvent event) {
        notificationService.sendPaymentCancelNotification(event.getUser(), event.getOrderName(), event.getRefundPrice());
    }
}
