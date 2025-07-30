package org.example.sansam.notification.event;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class NotificationEvent {
    private final Long userId;
    private final Object payload;
}
