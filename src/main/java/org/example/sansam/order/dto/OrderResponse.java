package org.example.sansam.order.dto;


import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.sansam.order.domain.Order;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class OrderResponse {

    private String orderNumber;
    private String paymentKey;
    private int totalAmount;
    private String status;
    private LocalDateTime createdAt;

    public OrderResponse(Order order){
        this.orderNumber = order.getOrderNumber();
        this.paymentKey = order.getPaymentKey();
        this.totalAmount = order.getTotalAmount();
        this.status = order.getStatus().toString();
        this.createdAt = order.getCreatedAt();
    }


}
