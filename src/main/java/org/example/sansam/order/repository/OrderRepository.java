package org.example.sansam.order.repository;


import org.example.sansam.notification.dto.ReviewRequestDTO;
import org.example.sansam.order.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
@Repository
public interface OrderRepository extends JpaRepository<Order,Long> {
}
