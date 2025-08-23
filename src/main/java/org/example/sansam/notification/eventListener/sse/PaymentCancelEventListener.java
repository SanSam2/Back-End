package org.example.sansam.notification.eventListener.sse;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.sansam.notification.event.sse.PaymentCancelEvent;
import org.example.sansam.notification.service.NotificationService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Log4j2
public class PaymentCancelEventListener {

    private final NotificationService notificationService;

    @EventListener
    public void handelPaymentCancelEvent(PaymentCancelEvent event) {
        try{
            notificationService.sendPaymentCancelNotification(event.getUser(), event.getOrderName(), event.getRefundPrice());
        }catch (Exception e){
            log.error("결제 취소 알림 전송 실패 - userId={}", event.getUser() != null ? event.getUser().getId() : "null", e);
        }
    }
}
