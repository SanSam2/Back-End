package org.example.sansam.notification.event.sse;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.sansam.chat.domain.ChatRoom;
import org.example.sansam.user.domain.User;

@Getter
@RequiredArgsConstructor
public class ChatEvent {
    private final ChatRoom chatRoom;
    private final User user;
    private final String message;
}
