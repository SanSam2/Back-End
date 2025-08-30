package org.example.sansam.order.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemDto {

    private Long productId;
    private String productName;
    private Long productPrice;
    private String productSize;
    private String productColor;
    private int quantity;

    public OrderItemDto(long productId , String productName, Long productPrice,
                        String productSize, String productColor, int quantity) {
        this.productId = productId;
        this.productName = productName;
        this.productPrice = productPrice;
        this.productSize = productSize;
        this.productColor = productColor;
        this.quantity = quantity;
    }
}
