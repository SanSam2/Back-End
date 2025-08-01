package org.example.sansam.notification.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.sansam.user.domain.User;

@Getter
@AllArgsConstructor
public class PaymentCompleteEvent {
    private final User user;
    private final String orderName;
    private final Long orderPrice;
}
