package org.example.sansam.notification.eventListener.sse;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.sansam.notification.event.sse.PaymentCancelEvent;
import org.example.sansam.notification.service.NotificationService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Log4j2
public class PaymentCancelEventListener {

    private final NotificationService notificationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handelPaymentCancelEvent(PaymentCancelEvent event) {
        try{
            notificationService.sendPaymentCancelNotification(event.getUser(), event.getOrderName(), event.getRefundPrice());
        }catch (Exception e){
            log.error("결제 취소 알림 전송 실패 - userId={}", event.getUser() != null ? event.getUser().getId() : "null", e);
        }
    }
}
