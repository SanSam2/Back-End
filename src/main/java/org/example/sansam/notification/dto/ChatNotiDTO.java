package org.example.sansam.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChatNotiDTO {
    private Long senderId;
    private String senderName;
    private String message;
}
