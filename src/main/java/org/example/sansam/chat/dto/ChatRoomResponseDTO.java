package org.example.sansam.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.sansam.chat.domain.ChatRoom;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoomResponseDTO {

    private Long id;
    private String roomName;
    private LocalDateTime createdAt;
    private LocalDateTime lastMessageAt;


    public static ChatRoomResponseDTO fromEntity(ChatRoom chatRoom) {
        return ChatRoomResponseDTO.builder()
                .id(chatRoom.getId())
                .roomName(chatRoom.getRoomName())
                .createdAt(chatRoom.getCreatedAt())
                .lastMessageAt(chatRoom.getLastMessageAt())
                .build();
    }
}