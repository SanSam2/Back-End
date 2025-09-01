package org.example.sansam.order.repository;

import org.example.sansam.order.domain.Order;
import org.example.sansam.order.domain.OrderProduct;
import org.example.sansam.status.domain.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


public interface OrderRepositoryCustom {
    List<Order> searchOrderByUserId(Long userId);
    Order findOrderByOrderNumber(String orderNumber);
    Optional<OrderProduct> findReviewTarget(Long userId, String orderNumber, Long orderProductId);
    Page<Long> pageOrderIdsByUserId(Long userId, Pageable pageable);
    List<Order> findOrdersWithItemsByIds(List<Long> ids);

    int deleteExpiredWaitingOrders(Status status, LocalDateTime expiredAt);
    List<Long> findExpiredWaitingOrderIds(Status waiting, LocalDateTime expiredAt, int limit);
    Optional<Order> findByIdWithItems(Long id);
}
