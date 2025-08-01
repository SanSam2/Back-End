package org.example.sansam.notification.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.sansam.user.domain.User;

@Getter
@AllArgsConstructor
public class ReviewRequestEvent {
    private final User user;
    private final String productName;
}
