package org.example.sansam.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoomRequestDTO {

    private Long id;
    private String roomName;
    private LocalDateTime createdAt;

}
