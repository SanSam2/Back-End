package org.example.sansam.notification.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.sansam.chat.domain.ChatRoom;
import org.example.sansam.user.domain.User;

@Getter
@AllArgsConstructor
public class ChatEvent {
    private final ChatRoom chatRoom;
    private final User user;
    private final String message;
}
