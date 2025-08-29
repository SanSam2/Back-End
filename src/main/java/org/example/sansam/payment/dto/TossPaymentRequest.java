package org.example.sansam.payment.dto;


import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TossPaymentRequest {

    private String paymentKey;
    private String orderId; //orderNumber
    private Long amount;

    public TossPaymentRequest(String paymentKey, String orderId, Long amount) {
        this.paymentKey = paymentKey;
        this.orderId = orderId;
        this.amount = amount;
    }
}
