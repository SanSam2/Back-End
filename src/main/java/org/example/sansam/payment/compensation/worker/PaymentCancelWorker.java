package org.example.sansam.payment.compensation.worker;


import lombok.RequiredArgsConstructor;
import org.example.sansam.payment.compensation.domain.PaymentCancelOutBox;
import org.example.sansam.payment.compensation.repository.PaymentCancelOutBoxRepository;
import org.example.sansam.payment.service.PaymentApiClient;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

@Component
@RequiredArgsConstructor
public class PaymentCancelWorker {

    private final PaymentCancelOutBoxRepository repo;
    private final PaymentCancelProcessor processor;

    @Scheduled(fixedDelay = 30000L, initialDelay = 10000L)
    public void run() {
        String workerId = "cancel-worker-" + UUID.randomUUID();

        // 한 번에 너무 많이 집어오지 않도록 페이징
        List<PaymentCancelOutBox> list = repo.findRunnable(LocalDateTime.now(), PageRequest.of(0, 50));

        for (PaymentCancelOutBox job : list) {
            // 1) Claim (경쟁 방지)
            int updated = repo.claim(job.getId(), workerId, LocalDateTime.now());
            if (updated == 0)
                continue; // 누군가가 먼저 잡았음

            // 2) 각 잡 처리는 별도 트랜잭션으로 (부분 실패/성공 분리)
            processor.processOne(job.getId());
        }
    }
}
