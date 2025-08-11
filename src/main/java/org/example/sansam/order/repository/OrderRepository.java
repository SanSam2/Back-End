package org.example.sansam.order.repository;


import jakarta.transaction.Transactional;
import org.example.sansam.order.domain.Order;
import org.example.sansam.status.domain.Status;
import org.example.sansam.status.domain.StatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.*;

@Repository
public interface OrderRepository extends JpaRepository<Order,Long> {
    List<Order> findByUser_Id(Long userId);
    Optional<Order> findByPaymentKey(String paymentKey);
    Optional<Order> findByOrderNumber(String orderNumber);  // 이 메소드 추가

    //취소할때, OrderNumber로 order찾아서 처리해야하기에
    @Query("select o from Order o join fetch o.orderProducts where o.orderNumber = :orderNumber")
    Order findOrderWithProducts(@Param("orderNumber") String orderNumber);



    //계속 order_status가 waiting으로 되어있는 부분에 대한 삭제 처리
    @Transactional
    @Modifying
    @Query("delete from Order o where o.status = :status and o.createdAt < :expiredTime")
    int deleteExpiredWaitingOrders(@Param("status") Status status,
                                   @Param("expiredTime") LocalDateTime expiredTime);

    //페이징을 위한 쿼리문
    @Query("""
      select o.id
      from Order o
      where o.user.id = :userId
        and o.createdAt >= :from
      order by o.createdAt desc, o.id desc
    """)
    Page<Long> findRecentOrderIds(@Param("userId") Long userId,
                                  @Param("from") LocalDateTime from,
                                  Pageable pageable);

    // ids로 2차 조회: 필요한 연관 전부 fetch
    @Query("""
      select distinct o
      from Order o
      join fetch o.user u
      join fetch o.status os
      left join fetch o.orderProducts op
      left join fetch op.product p
      left join fetch op.status ops
      where o.id in :ids
    """)
    List<Order> findOrdersWithProductsFetchJoin(@Param("ids") List<Long> ids);

    List<Order> findByDeliveredAtBeforeAndStatus_StatusId(LocalDateTime deliveredAtBefore, Long statusId);
}
