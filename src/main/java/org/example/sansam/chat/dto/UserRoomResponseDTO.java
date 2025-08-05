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
public class UserRoomResponseDTO {
    private Long id;
    private String roomName;
    private LocalDateTime createdAt;
    private LocalDateTime lastMessageAt;
    private Long messageCount;

    // chatRoom + unreadCount 로 DTO 생성
    public static UserRoomResponseDTO fromEntity(
            ChatRoom chatRoom, Long messageCount) {
        return UserRoomResponseDTO.builder()
                .id(chatRoom.getId())
                .roomName(chatRoom.getRoomName())
                .createdAt(chatRoom.getCreatedAt())
                .lastMessageAt(chatRoom.getLastMessageAt())
                .messageCount(messageCount)
                .build();
    }
}