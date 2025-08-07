package org.example.sansam.cart.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class deleteCartItem {
    private Long productId;
    private String color;
    private String size;
    private Long quantity;
}
