package org.example.sansam.order.repository;


import org.example.sansam.order.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface OrderRepository extends JpaRepository<Order,Long>,OrderRepositoryCustom {
    //select o from Order o where o.orderNumber = ?
    Optional<Order> findByOrderNumber(String orderNumber);
}
