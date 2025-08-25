package org.example.sansam.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.example.sansam.notification.domain.NotificationHistories;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Builder
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "알림 DTO")
public class NotificationDTO {
    @Schema(description = "알림 ID",example = "1001")
    private Long id;

    @Schema(description = "제목",example = "결제 완료")
    private String title;

    @Schema(description = "메시지 본문",example = "결제가 완료되었습니다.")
    private String message;

    @Schema(description = "전송 시간",example = "2025-08-10T14:02:00")
    private LocalDateTime createdAt;

    @Schema(description = "읽음 여부",example = "false")
    private boolean isRead;

    public static NotificationDTO from(NotificationHistories entity){
        return NotificationDTO.builder()
                .title(entity.getTitle())
                .message(entity.getMessage())
                .createdAt(entity.getCreatedAt())
                .isRead(entity.isRead())
                .build();
    }
}
