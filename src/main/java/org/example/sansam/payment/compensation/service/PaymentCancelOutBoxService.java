package org.example.sansam.payment.compensation.service;


import lombok.RequiredArgsConstructor;
import org.example.sansam.payment.compensation.domain.PaymentCancelOutBox;
import org.example.sansam.payment.compensation.repository.PaymentCancelOutBoxRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

@Service
@RequiredArgsConstructor
public class PaymentCancelOutBoxService {

    private final PaymentCancelOutBoxRepository paymentCancelOutBoxRepository;

    @Transactional(propagation = REQUIRES_NEW)
    public void enqueue(String paymentKey, long amount, String reason, String idemKey) {
        PaymentCancelOutBox outBox = PaymentCancelOutBox.create(paymentKey, amount, reason, idemKey);
        paymentCancelOutBoxRepository.save(outBox);
    }

}
