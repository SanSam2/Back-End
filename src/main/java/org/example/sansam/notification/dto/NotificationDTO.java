package org.example.sansam.notification.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.example.sansam.notification.domain.NotificationHistories;
import org.example.sansam.notification.exception.CustomException;
import org.example.sansam.notification.exception.ErrorCode;

import java.time.LocalDateTime;

@Builder
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "알림 DTO")
public class NotificationDTO {
    @Schema(description = "알림 ID", example = "1001")
    private Long id;

    @Schema(description = "알림 eventName", example = "welcomeMessage")
    private String eventName;

    @Schema(description = "제목", example = "결제 완료")
    private String title;

    @Schema(description = "메시지 본문", example = "결제가 완료되었습니다.")
    private String message;

    @Schema(description = "전송 시간", example = "2025-08-10T14:02:00")
    private LocalDateTime createdAt;

    @Schema(description = "읽음 여부", example = "false")
    private boolean isRead;

    public static NotificationDTO from(NotificationHistories entity) {
        return NotificationDTO.builder()
                .id(entity.getId())
                .eventName(entity.getEventName())
                .title(entity.getTitle())
                .message(entity.getMessage())
                .createdAt(entity.getCreatedAt())
                .isRead(entity.isRead())
                .build();
    }

    // 직렬화
    public String toJson() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorCode.NOTIFICATION_SERIALIZATION_FAILED);
        }
    }
}
