package org.example.sansam.order.repository.ordersimplequery;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderQueryRepository {

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


    @Query("""
      select
        o.id                        as orderId,
        o.orderNumber               as orderNumber,
        o.totalAmount               as totalAmount,
        o.createdAt                 as createdAt,
        os.statusName               as orderStatus,

        op.id                       as orderProductId,
        op.quantity                 as quantity,
        ops.statusName              as orderProductStatus,

        op.orderedProductPrice      as orderedProductPrice,
        op.orderedProductSize       as orderedProductSize,
        op.orderedproductColor      as orderedProductColor,

        op.product.id               as productId

      from Order o
      join o.status os
      left join o.orderProducts op
      left join op.status ops
      where o.id in :ids
      order by o.createdAt desc, o.id desc, op.id asc
    """)
    List<OrderRowProjection> findOrderRowsByIds(@Param("ids") List<Long> ids);

    interface OrderRowProjection {
        Long getOrderId();
        String getOrderNumber();
        Long getTotalAmount();
        LocalDateTime getCreatedAt();
        String getOrderStatus();

        Long getOrderProductId();
        Integer getQuantity();
        String getOrderProductStatus();

        Long getOrderedProductPrice();
        String getOrderedProductSize();
        String getOrderedProductColor();

        Long getProductId();

        // String getOrderedProductName();
        // String getOrderedProductThumb();
    }
}
