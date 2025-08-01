package org.example.sansam.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.sql.Timestamp;

@Getter
@AllArgsConstructor
public class PaymentCancelNotiDTO {
    private Long id;
    private Long orderId;
    private Long refundPrice;
}
