package org.example.sansam.notification.event.sse;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class BroadcastEvent {
    private final String eventName;
    private final String payloadJson;
}
