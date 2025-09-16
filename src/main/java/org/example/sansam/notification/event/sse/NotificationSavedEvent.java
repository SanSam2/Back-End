package org.example.sansam.notification.event.sse;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class NotificationSavedEvent {
    private final Long userId;
    private final Long nhId;
    private final String eventName;
    private final String payloadJson;

    public static NotificationSavedEvent of(Long userId, Long nhId, String eventName, String payloadJson) {
        return new NotificationSavedEvent(userId, nhId, eventName, payloadJson);
    }
}
