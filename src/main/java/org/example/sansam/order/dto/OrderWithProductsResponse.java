package org.example.sansam.order.dto;

import lombok.Getter;
import org.example.sansam.status.Status;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class OrderWithProductsResponse {
    private String orderNumber;
    private Long totalAmount;
    private LocalDateTime createdAt;
    private String orderStatus;
    private List<ProductSummary> items;


    public OrderWithProductsResponse(String orderNumber, Long totalAmount, LocalDateTime createdAt, String orderStatus, List<ProductSummary> items) {
        this.orderNumber = orderNumber;
        this.totalAmount = totalAmount;
        this.createdAt = createdAt;
        this.orderStatus = orderStatus;
        this.items = items;
    }

    @Getter
    public static class ProductSummary {
        private Long orderProductId;
        private Long productId;
        private String productName;
        private Long productPrice;
        private int quantity;
        private String orderProductStatus;

        public ProductSummary(Long orderProductId, Long productId, String productName, Long productPrice, int quantity, String orderProductStatus) {
            this.orderProductId = orderProductId;
            this.productId = productId;
            this.productName = productName;
            this.productPrice = productPrice;
            this.quantity = quantity;
            this.orderProductStatus = orderProductStatus;
        }
    }
}
