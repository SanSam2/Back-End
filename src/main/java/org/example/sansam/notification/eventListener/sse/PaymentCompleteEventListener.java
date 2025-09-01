package org.example.sansam.notification.eventListener.sse;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.sansam.notification.event.sse.PaymentCompleteEvent;
import org.example.sansam.notification.service.NotificationService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Log4j2
public class PaymentCompleteEventListener {

    private final NotificationService notificationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handelPaymentCompleteEvent(PaymentCompleteEvent event) {
        try {
            notificationService.sendPaymentCompleteNotification(event.getUser(), event.getOrderName(), event.getOrderPrice());
        }catch (Exception e){
            log.error("결제 완료 알림 전송 실패 - userId={}", event.getUser() != null ? event.getUser().getId() : "null", e);
        }
    }
}
