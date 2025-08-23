package org.example.sansam.notification.eventListener.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.sansam.notification.event.email.PaymentCompleteEmailEvent;
import org.example.sansam.notification.service.EmailService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentCompleteEmailEventListener {
    private final EmailService emailService;

    @Async
    @EventListener
    public void handlePaymentCompleted(PaymentCompleteEmailEvent event) {
        try {
            emailService.sendPaymentCompletedEmail(event.getUser(), event.getOrderName(), event.getFinalPrice());
        } catch (Exception e) {
            log.error("결제 완료 메일 전송 실패 - userId={}", event.getUser() != null ? event.getUser().getId() : "null", e);
        }
    }
}
