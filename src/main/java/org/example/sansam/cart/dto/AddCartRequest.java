package org.example.sansam.cart.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddCartRequest {
    private Long userId;
    private Long quantity;
    private Long productDetailsId;
    private String url;
}
