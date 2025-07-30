package org.example.sansam.notification.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WishListLowEvent {
    private final Long userId;
    private final String productName;
}
