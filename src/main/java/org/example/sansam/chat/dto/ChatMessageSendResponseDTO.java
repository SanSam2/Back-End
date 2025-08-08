package org.example.sansam.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.sansam.chat.domain.ChatMessage;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageSendResponseDTO {

    private Long id;
    private String message;
    private LocalDateTime createdAt;
    private String userName;
    private Long sender;
    private Long roomId;

    public static ChatMessageSendResponseDTO fromEntity(ChatMessage chatMessage, String userName, Long roomId, Long userId) {
        return ChatMessageSendResponseDTO.builder()
                .id(chatMessage.getId())
                .message(chatMessage.getMessage())
                .createdAt(chatMessage.getCreatedAt())
                .userName(userName)
                .sender(userId)
                .roomId(roomId)
                .build();
    }
}