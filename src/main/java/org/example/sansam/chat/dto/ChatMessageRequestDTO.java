package org.example.sansam.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageRequestDTO {

    @NotBlank(message = "메시지는 비어있을 수 없습니다")
    @Size(max = 1000, message = "메시지는 1000자를 초과할 수 없습니다")
    private String message;

}