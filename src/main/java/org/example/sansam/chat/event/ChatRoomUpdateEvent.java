package org.example.sansam.chat.event;

import java.time.LocalDateTime;

public class ChatRoomUpdateEvent {

    private final Long roomId;
    private final LocalDateTime lastMessageAt;

    public ChatRoomUpdateEvent(Long roomId, LocalDateTime lastMessageAt) {
        this.roomId = roomId;
        this.lastMessageAt = lastMessageAt;
    }

    public Long getRoomId() { return roomId; }
    public LocalDateTime getLastMessageAt() { return lastMessageAt; }
}
