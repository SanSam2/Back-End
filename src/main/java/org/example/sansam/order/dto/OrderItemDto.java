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

}
