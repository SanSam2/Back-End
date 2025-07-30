package org.example.sansam.order.dto;

import lombok.Getter;

@Getter
public class OrderItemDto {

    private Long productId;
    private int productPrice;
    private int quantity;

}
