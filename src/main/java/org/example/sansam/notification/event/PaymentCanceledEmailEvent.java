package org.example.sansam.notification.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.sansam.user.domain.User;

@Getter
@RequiredArgsConstructor
public class PaymentCanceledEmailEvent {
    private final User user;
    private final String orderName;
    private final Long refundPrice;
}
