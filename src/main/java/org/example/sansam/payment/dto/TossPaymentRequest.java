package org.example.sansam.payment.dto;


import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TossPaymentRequest {

    private String paymentKey;
    private String orderId; //orderNumber
    private Long amount;

}
