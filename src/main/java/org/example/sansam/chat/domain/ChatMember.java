package org.example.sansam.chat.domain;

import jakarta.persistence.*;
import lombok.*;
import org.example.sansam.user.domain.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_member")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMember {

    @EmbeddedId
    private ChatMemberId id;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @MapsId("chatRoomId")
    @JoinColumn(name = "chatroom_id")
    private ChatRoom chatRoom;

    @Column(name = "last_read_at")
    private LocalDateTime lastReadAt;
}