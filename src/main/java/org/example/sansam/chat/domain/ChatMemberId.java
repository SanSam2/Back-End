package org.example.sansam.chat.domain;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.Serializable;


@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class ChatMemberId implements Serializable {
    private Long userId;
    private Long chatRoomId;

}
