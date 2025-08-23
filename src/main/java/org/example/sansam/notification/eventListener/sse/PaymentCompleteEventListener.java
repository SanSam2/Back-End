package org.example.sansam.notification.eventListener.sse;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.sansam.notification.event.sse.PaymentCompleteEvent;
import org.example.sansam.notification.service.NotificationService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Log4j2
public class PaymentCompleteEventListener {

    private final NotificationService notificationService;

    @EventListener
    public void handelPaymentCompleteEvent(PaymentCompleteEvent event) {
        try {
            notificationService.sendPaymentCompleteNotification(event.getUser(), event.getOrderName(), event.getOrderPrice());
        }catch (Exception e){
            log.error("결제 완료 알림 전송 실패 - userId={}", event.getUser() != null ? event.getUser().getId() : "null", e);
        }
    }
}
