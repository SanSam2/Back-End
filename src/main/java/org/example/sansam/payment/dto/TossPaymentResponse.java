package org.example.sansam.payment.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class TossPaymentResponse {

    String method;
    long totalAmount;
    long finalAmount;
    LocalDateTime approvedAt;

}
