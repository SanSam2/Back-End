package org.example.sansam.order.dto;


import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.sansam.order.domain.Order;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class OrderResponse {



    private Long orderId;
    private String orderNumber;
    private String orderName;
    private Long totalAmount;
    private String status;
    private LocalDateTime createdAt;

    public OrderResponse(Order order){
        this.orderId = order.getId();
        this.orderName= order.getOrderName();
        this.orderNumber = order.getOrderNumber();
        this.totalAmount = order.getTotalAmount();
        this.status = order.getStatus().toString();
        this.createdAt = order.getCreatedAt();
    }


}
