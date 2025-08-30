package org.example.sansam.payment.dto;

import lombok.Getter;

@Getter
public class CancelProductRequest {

    private Long orderProductId;
    private int cancelQuantity;

    public CancelProductRequest(Long orderProductId, int cancelQuantity) {
        this.orderProductId = orderProductId;
        this.cancelQuantity = cancelQuantity;
    }
}
