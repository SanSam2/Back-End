package org.example.sansam.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
public class RoomCountDTO {
    private Long roomId;
    private Long count;
}