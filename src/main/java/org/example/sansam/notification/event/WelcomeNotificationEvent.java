package org.example.sansam.notification.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.sansam.user.domain.User;

@Getter
@RequiredArgsConstructor
public class WelcomeNotificationEvent {
    private final User user;
}
