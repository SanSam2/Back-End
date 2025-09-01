package org.example.sansam.payment.compensation.worker;


import lombok.RequiredArgsConstructor;
import org.example.sansam.payment.compensation.domain.PaymentCancelOutBox;
import org.example.sansam.payment.compensation.repository.PaymentCancelOutBoxRepository;
import org.example.sansam.payment.service.PaymentApiClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

@Service
@RequiredArgsConstructor
public class PaymentCancelProcessor {

    private final PaymentCancelOutBoxRepository repo;
    private final PaymentApiClient paymentApiClient;


    @Transactional(propagation = REQUIRES_NEW)
    public void processOne(Long id) {
        PaymentCancelOutBox job = repo.findById(id).orElse(null);
        if (job == null)
            return;

        try {
            paymentApiClient.tossPaymentCancel(job.getPaymentKey(), job.getAmount(), job.getReason(),job.getIdempotencyKey());
            job.markSucceeded();
        } catch (Exception ex) {
            job.markFailedAndScheduleRetry(ex.getMessage());
        }
    }
}
