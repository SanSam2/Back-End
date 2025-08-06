package org.example.sansam.notification.eventListener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.sansam.notification.event.PaymentCanceledEmailEvent;
import org.example.sansam.notification.service.EmailService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.EnumMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentCanceledEmailEventListener {
    private final EmailService emailService;

    @Async
    @EventListener
    public void handlePaymentCanceled(PaymentCanceledEmailEvent event) {
        emailService.sendPaymentCanceledMessage(event.getUser(), event.getOrderName(), event.getRefundPrice());
    }
}
