package org.example.sansam.order.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.sansam.order.compensation.worker.OrderExpiryProcessor;
import org.example.sansam.order.repository.OrderRepository;
import org.example.sansam.status.domain.Status;
import org.example.sansam.status.domain.StatusEnum;
import org.example.sansam.status.service.StatusCachingService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderCleanUpScheduler {

    private final OrderRepository orderRepository;
    private final StatusCachingService statusCachingService;
    private final OrderExpiryProcessor orderExpiryProcessor;

    @Scheduled(cron = "0 * * * * *")
    public void cleanUpExpiredOrders() {
        Status orderWaiting = statusCachingService.get(StatusEnum.ORDER_WAITING);
        Status orderExpired = statusCachingService.get(StatusEnum.ORDER_EXPIRED);
        LocalDateTime expiredtime = LocalDateTime.now().minusMinutes(10);


        List<Long> ids = orderRepository.findExpiredWaitingOrderIds(orderWaiting, expiredtime, 500);
        if (ids.isEmpty()) {
            log.info("[OrderCleanUp] expired=0");
            return;
        }

        int ok=0, fail=0, enque=0;
        for (Long id : ids) {
            try {
                enque += orderExpiryProcessor.expireAndEnqueue(id, orderExpired); // 1건이 1 트랜잭션이어야하니까
                ok++;
            } catch (Exception e) {
                fail++;
                log.warn("[OrderCleanUp] failed orderId={}", id, e);
            }
        }

        log.info("[만료된 주문] ok={}, fail={}, enqueuedRestores={}", ok, fail, enque);

    }
}
