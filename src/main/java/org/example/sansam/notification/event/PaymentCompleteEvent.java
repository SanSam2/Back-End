package org.example.sansam.notification.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.sansam.notification.dto.PaymentNotiDTO;

@Getter
@AllArgsConstructor
public class PaymentCompleteEvent {
    private final Long userId;
    private final String orderName;
    private final Long orderPrice;
}
