package org.example.sansam.order.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemResponseDto {

    private Long productId;
    private String productName;
    private Long productPrice;
    private String productSize;
    private String productColor;
    private int quantity;
    private String orderProductImageUrl;


    public OrderItemResponseDto(Long productId, String productName, Long productPrice, String productSize, String productColor, int quantity, String orderProductImageUrl) {
        this.productId = productId;
        this.productName = productName;
        this.productPrice = productPrice;
        this.productSize = productSize;
        this.productColor = productColor;
        this.quantity = quantity;
        this.orderProductImageUrl = orderProductImageUrl;
    }
}
