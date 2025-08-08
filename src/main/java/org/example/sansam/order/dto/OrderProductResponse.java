package org.example.sansam.order.dto;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class OrderProductResponse {
    private Long orderId;
    private LocalDateTime createdAt;
    private String orderStatus;

    private Long orderProductId;
    private Long productId;
    private String productName;
    private Long productPrice;

    private int quantity;
    private String orderProductStatus;

    // 생성자
    public OrderProductResponse(
            Long orderId,
            Long userId,
            LocalDateTime createdAt,
            String orderStatus,
            Long orderProductId,
            Long productId,
            String productName,
            Long productPrice,
            int quantity,
            String orderProductStatus
    ) {
        this.orderId = orderId;
        this.createdAt = createdAt;
        this.orderStatus = orderStatus;
        this.orderProductId = orderProductId;
        this.productId = productId;
        this.productName = productName;
        this.productPrice = productPrice;
        this.quantity = quantity;
        this.orderProductStatus = orderProductStatus;
    }
}
