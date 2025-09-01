package org.example.sansam.order.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.sansam.order.compensation.domain.StockRestoreOutBox;
import org.example.sansam.order.compensation.service.StockRestoreOutBoxService;
import org.example.sansam.order.compensation.worker.OrderExpiryProcessor;
import org.example.sansam.order.domain.Order;
import org.example.sansam.order.domain.OrderProduct;
import org.example.sansam.order.repository.OrderRepository;
import org.example.sansam.product.service.ProductService;
import org.example.sansam.status.domain.Status;
import org.example.sansam.status.domain.StatusEnum;
import org.example.sansam.status.repository.StatusRepository;
import org.example.sansam.stock.Service.StockService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderCleanUpScheduler {

    private final OrderRepository orderRepository;
    private final StatusRepository statusRepository;
    private final OrderExpiryProcessor orderExpiryProcessor;
    private final StockRestoreOutBoxService stockOutBoxService;
    private final ProductService productService;

    @Scheduled(cron = "0 * * * * *")
    public void cleanUpExpiredOrders() {
        Status orderWaiting = statusRepository.findByStatusName(StatusEnum.ORDER_WAITING);
        Status orderExpired = statusRepository.findByStatusName(StatusEnum.ORDER_EXPIRED);
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
