package org.example.sansam.notification.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PaymentCancelEvent {
    private final Long userId;
    private final String orderName;
    private final Long refundPrice;
}
