package org.example.sansam.notification.event.email;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.sansam.user.domain.User;

@RequiredArgsConstructor
@Getter
public class UserWelcomeEmailEvent {
    private final User user;
}
