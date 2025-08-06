package org.example.sansam.notification.dto;

import lombok.*;
import org.example.sansam.notification.domain.NotificationHistories;

import java.sql.Timestamp;

@Builder
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class NotificationDTO {

    private Long id;
    private String title;
    private String message;
    private Timestamp createdAt;
    private boolean isRead;

    public static NotificationDTO from(NotificationHistories entity){
        return NotificationDTO.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .message(entity.getMessage())
                .createdAt(entity.getCreatedAt())
                .isRead(entity.isRead())
                .build();
    }
}
