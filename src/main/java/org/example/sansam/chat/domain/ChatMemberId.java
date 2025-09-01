package org.example.sansam.chat.domain;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;


@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class ChatMemberId implements Serializable {
    private Long userId;
    private Long chatRoomId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChatMemberId)) return false;
        ChatMemberId that = (ChatMemberId) o;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(chatRoomId, that.chatRoomId);
    }

}