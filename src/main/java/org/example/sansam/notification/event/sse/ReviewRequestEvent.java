package org.example.sansam.notification.event.sse;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.sansam.user.domain.User;

@Getter
@RequiredArgsConstructor
public class ReviewRequestEvent {
    private final User user;
    private final String orderName;
}
