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
public class ChatMessageResponseDTO {

    private Long id;
    private String message;
    private Boolean isRead;
    private LocalDateTime createdAt;

    public static ChatMessageResponseDTO fromEntity(ChatMessage chatMessage) {
        return ChatMessageResponseDTO.builder()
                .id(chatMessage.getId())
                .message(chatMessage.getMessage())
                .isRead(chatMessage.getIsRead())
                .createdAt(chatMessage.getCreatedAt())
                .build();
    }
}