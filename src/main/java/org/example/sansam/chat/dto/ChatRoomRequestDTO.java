package org.example.sansam.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
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

    @NotBlank( message = "방 이름은 필수 값입니다.")
    private String roomName;

    private LocalDateTime createdAt;

    @PositiveOrZero(message = "금액은 0 이상이어야 합니다.")
    private Long setAmount;

}
