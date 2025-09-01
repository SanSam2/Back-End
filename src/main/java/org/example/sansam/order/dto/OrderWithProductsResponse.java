package org.example.sansam.order.dto;

import lombok.Builder;
import lombok.Getter;
import org.example.sansam.status.domain.StatusEnum;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class OrderWithProductsResponse {
    private String orderNumber;
    private Long totalAmount;
    private LocalDateTime createdAt;
    private StatusEnum orderStatus;
    private List<ProductSummary> items;


    public OrderWithProductsResponse(String orderNumber, Long totalAmount, LocalDateTime createdAt, StatusEnum orderStatus, List<ProductSummary> items) {
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
        private String productSize;
        private String productColor;
        private Long productPrice;
        private int quantity;
        private String orderProductImageUrl;
        private StatusEnum orderProductStatus;


        public ProductSummary(Long orderProductId, Long productId, String productName, Long productPrice,
                              String productSize, String productColor, int quantity,String orderProductImageUrl, StatusEnum orderProductStatus) {
            this.orderProductId = orderProductId;
            this.productId = productId;
            this.productName = productName;
            this.productSize = productSize;
            this.productColor = productColor;
            this.productPrice = productPrice;
            this.quantity = quantity;
            this.orderProductImageUrl = orderProductImageUrl;
            this.orderProductStatus = orderProductStatus;

        }
    }
}
