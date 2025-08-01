package org.example.sansam.notification.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReviewRequestEvent {
    private final Long userId;
    private final String username;
    private final String productName ;
}
