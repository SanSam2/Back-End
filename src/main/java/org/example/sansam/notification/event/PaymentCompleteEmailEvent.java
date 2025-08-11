package org.example.sansam.notification.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.sansam.user.domain.User;

@Getter
@RequiredArgsConstructor
public class PaymentCompleteEmailEvent {
    private final User user;
    private final String orderName;
    private final Long finalPrice;
}
