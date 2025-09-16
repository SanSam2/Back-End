package org.example.sansam.order.compensation.worker;

import lombok.RequiredArgsConstructor;
import org.example.sansam.order.compensation.service.StockRestoreOutBoxService;
import org.example.sansam.order.domain.Order;
import org.example.sansam.order.domain.OrderProduct;
import org.example.sansam.order.repository.OrderRepository;
import org.example.sansam.product.service.ProductService;
import org.example.sansam.status.domain.Status;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderExpiryProcessor {

    private final OrderRepository orderRepository;
    private final ProductService productService;
    private final StockRestoreOutBoxService stockOutBoxService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int expireAndEnqueue(Long orderId, Status orderExpired) {
        // 트랜잭션 안에서 다시
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new IllegalStateException("order not found: " + orderId));

        // 이미 EXPIRED면 스킵

        order.changeStatusMarkExpired(orderExpired);

        int enqueued = 0;
        for (OrderProduct item : order.getOrderProducts()) {
            Long detailId = productService.getDetailId(
                    item.getOrderedProductColor(), item.getOrderedProductSize(), item.getProduct().getId()
            );
            stockOutBoxService.enqueue(
                    order.getId(),
                    detailId,
                    item.getQuantity(),
                    "EXPIRE:" + order.getOrderNumber() + ":" + item.getId()
            );
            enqueued++;
        }
        return enqueued;
    }

}
