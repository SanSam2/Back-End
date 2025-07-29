package org.example.sansam.cart.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeleteCartRequest {
    private Long userId;
    private Long productDetailsId;
    private Long quantity;
}
