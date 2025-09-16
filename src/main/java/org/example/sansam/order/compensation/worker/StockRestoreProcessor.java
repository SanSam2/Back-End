package org.example.sansam.order.compensation.worker;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.sansam.global.event.StockIncreaseRequestedEvent;
import org.example.sansam.order.compensation.domain.StockRestoreOutBox;
import org.example.sansam.order.compensation.repository.StockRestoreOutBoxRepository;
import org.example.sansam.order.publish.StockEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockRestoreProcessor {

    private final StockRestoreOutBoxRepository repo;
    private final StockEventPublisher stockEventPublisher;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void process(Long id) {
        log.info("[StockRestore] start id={} txActive={}", id,
                TransactionSynchronizationManager.isActualTransactionActive());

        StockRestoreOutBox job = repo.findById(id).orElse(null);
        if (job == null)
            return;

        try {
            StockIncreaseRequestedEvent event = StockIncreaseRequestedEvent.of(job.getIdempotencyKey(),job.getProductDetailId(),job.getQuantity());
            stockEventPublisher.publishIncreaseRequested(event);
            job.markSucceeded();
            repo.saveAndFlush(job);

            log.info("[StockRestore] success id={} detail={} qty={}",
                    id, job.getProductDetailId(), job.getQuantity());
        } catch (Exception e) {
            job.markFailedAndScheduleRetry(e.getMessage());
            repo.saveAndFlush(job);
            log.warn("[StockRestore] retry id={} attempt={} nextRunAt={} err={}",
                    id, job.getAttempt(), job.getNextRunAt(), e.toString());
        }
    }
}
