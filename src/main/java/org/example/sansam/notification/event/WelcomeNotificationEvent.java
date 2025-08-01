package org.example.sansam.notification.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.sansam.notification.dto.UserNotiDTO;
import org.springframework.stereotype.Component;

@Getter
@AllArgsConstructor
public class WelcomeNotificationEvent {
    private final Long userId;
    private final String username;
}
