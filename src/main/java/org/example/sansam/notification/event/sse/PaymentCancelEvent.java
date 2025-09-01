package org.example.sansam.notification.event.sse;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.sansam.user.domain.User;

@Getter
@RequiredArgsConstructor
public class PaymentCancelEvent {
    private final User user;
    private final String orderName;
    private final Long refundPrice;
}
