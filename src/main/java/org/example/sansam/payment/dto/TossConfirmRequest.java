package org.example.sansam.payment.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TossConfirmRequest {

    String paymentKey;
    Long orderId; //사실상 orderName이어야하는거 아닌가
    Long amount;
}
