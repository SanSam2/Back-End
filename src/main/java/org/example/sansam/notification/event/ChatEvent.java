package org.example.sansam.notification.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChatEvent {
    private final Long userId;
    private final Long senderId;
    private final String senderName;
}
