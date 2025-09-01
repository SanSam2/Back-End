package org.example.sansam.order.compensation.worker;

import lombok.RequiredArgsConstructor;
import org.example.sansam.order.compensation.repository.StockRestoreOutBoxRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
class StockRestoreWorker {
    private final StockRestoreOutBoxRepository repo;
    private final StockRestoreProcessor processor;

    @Scheduled(fixedDelay = 30000L, initialDelay = 10000L)
    public void run() {
        String workerId = "stock-restore-" + UUID.randomUUID();
        var jobs = repo.findRunnable(LocalDateTime.now(), PageRequest.of(0, 100));
        for (var j : jobs) {
            if (repo.claim(j.getId(), workerId, LocalDateTime.now()) == 0)
                continue;
            processor.process(j.getId());
        }
    }
}
