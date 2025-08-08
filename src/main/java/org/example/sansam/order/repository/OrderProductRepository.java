package org.example.sansam.order.repository;

import org.example.sansam.order.domain.OrderProduct;
import org.example.sansam.status.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import org.springframework.stereotype.Repository;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderProductRepository extends JpaRepository<OrderProduct, Long> {
    List<OrderProduct> findByOrder_Id(Long orderId);

    @Modifying
    @Transactional
    @Query("DELETE FROM OrderProduct op WHERE op.order.id IN (SELECT o.id FROM Order o WHERE o.status = :status AND o.createdAt < :expiredTime)")
    int deleteByOrderStatusAndCreatedAt(Status status, LocalDateTime expiredTime);


}
