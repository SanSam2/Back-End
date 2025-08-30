package org.example.sansam.order.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.sansam.order.repository.OrderRepository;
import org.example.sansam.status.domain.Status;
import org.example.sansam.status.domain.StatusEnum;
import org.example.sansam.status.repository.StatusRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderCleanUpScheduler {

    private final OrderRepository orderRepository;
    private final StatusRepository statusRepository;

    @Scheduled(cron = "0 0/10 * * * *")
    public void cleanUpExpiredOrders() {
        Status orderWaiting = statusRepository.findByStatusName(StatusEnum.ORDER_WAITING);

        LocalDateTime expiredtime = LocalDateTime.now().minusMinutes(30);
        int deletedCount = orderRepository.deleteExpiredWaitingOrders(orderWaiting, expiredtime);
        log.error(String.valueOf(deletedCount));
    }
}
