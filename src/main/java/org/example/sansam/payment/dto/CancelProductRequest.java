package org.example.sansam.payment.dto;

import lombok.Getter;

@Getter
public class CancelProductRequest {

    private Long productId;
    private int cancelQuantity;
}
