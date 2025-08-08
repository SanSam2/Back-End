package org.example.sansam.order.dto;

import lombok.Getter;
import lombok.Setter;
import org.example.sansam.order.domain.OrderProduct;

@Getter
@Setter
public class OrderItemDto {

    private Long productId;
    private Long productPrice;
    private String productSize;
    private String productColor;
    private int quantity;

}
