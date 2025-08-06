package org.example.sansam.order.repository;


import org.example.sansam.order.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;

@Repository
public interface OrderRepository extends JpaRepository<Order,Long> {
    List<Order> findByUser_Id(Long userId);
//    List<Order> findByDeliveredAtBeforeAndStatus_statusName(LocalDateTime date, String status);
}
