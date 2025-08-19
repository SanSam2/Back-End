package org.example.sansam.notification.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class NotificationSavedEvent {
    private final Long userId;
    private final String eventName;
    private final String payloadJson;

    public static NotificationSavedEvent of(Long userId, String eventName, String payloadJson) {
        return new NotificationSavedEvent(userId, eventName, payloadJson);
    }
}
