package org.example.sansam.notification.eventListener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.sansam.notification.event.PaymentCompleteEmailEvent;
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
        emailService.sendPaymentCompletedEmail(event.getUser(), event.getOrderName(), event.getFinalPrice());
    }
}
