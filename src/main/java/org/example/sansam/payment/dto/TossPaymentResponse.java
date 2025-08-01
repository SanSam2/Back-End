package org.example.sansam.payment.dto;


import lombok.Data;

@Data
public class TossPaymentResponse {

    private String orderId;
    private String paymentKey;
    private String method;
    private String status;
    private Long amount;
    private String requestedAt;
    private String approvedAt;
}
