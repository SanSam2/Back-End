package org.example.sansam.chat.domain;

import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
public class ChatMemberId implements Serializable {
    private Long userId;
    private Long chatRoomId;

}
