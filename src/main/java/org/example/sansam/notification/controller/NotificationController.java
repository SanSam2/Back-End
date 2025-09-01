package org.example.sansam.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.sansam.notification.dto.NotificationDTO;
import org.example.sansam.notification.exception.CustomException;
import org.example.sansam.notification.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
@Log4j2
@Tag(name = "Notifications", description = "알림(SSE/이력/읽음/삭제) API")
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(
            summary = "알림 이력 조회",
            description = "로그인 직후, 사용자의 알림 이력 목록을 반환합니다.",
            parameters = {
                    @Parameter(name = "userId", in = ParameterIn.PATH, required = true,
                            description = "사용자 ID", example = "1")
            }
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "알림 이력 반환",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = NotificationDTO.class),
                            examples = @ExampleObject(
                                    name = "ListExample",
                                    value = """
                                            [
                                              {
                                                "id": 1001,
                                                "title": "결제 완료",
                                                "message": "주문이 완료되었습니다.",
                                                "type": "PAYMENT_SUCCESS",
                                                "isRead": false,
                                                "createdAt": "2025-08-10T14:02:00"
                                              }
                                              ,
                                              {
                                                "id": 1002,
                                                "title": "결제 취소",
                                                "message": "결제가 취소되었습니다.",
                                                "type": "PAYMENT_CANCEL",
                                                "isRead": false,
                                                "createdAt": "2025-08-10T14:03:00"
                                              }
                                            ]
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "204", description = "알림 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("/list/{userId}")
    public ResponseEntity<List<NotificationDTO>> getNotificationList(@PathVariable Long userId) {
        List<NotificationDTO> histories = notificationService.getNotificationHistories(userId);
        if (histories.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(histories);
    }

    @Operation(
            summary = "읽지 않은 알림 개수 조회",
            description = "종 아이콘 배지에 표시할 미읽음 알림 개수를 반환합니다.",
            parameters = {
                    @Parameter(name = "userId", in = ParameterIn.PATH, required = true,
                            description = "사용자 ID", example = "1")
            }
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "개수 반환",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Long.class),
                            examples = @ExampleObject(value = "5")
                    )
            ),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("/unread-count/{userId}")
    public ResponseEntity<?> getUnreadNotificationCount(@PathVariable Long userId) {
        Long notificationCount = notificationService.getUnreadNotificationCount(userId);
        return ResponseEntity.ok(notificationCount);
    }

    @Operation(
            summary = "알림 단건 읽음 처리",
            description = "알림 카드 클릭 시 해당 알림을 읽음 처리합니다.",
            parameters = {
                    @Parameter(name = "notificationHistoriesId", in = ParameterIn.PATH, required = true,
                            description = "알림 이력 ID", example = "2")
            }
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "읽음 처리 성공"),
            @ApiResponse(responseCode = "404", description = "대상 없음(사용자 또는 알림 ID 유효하지 않음)"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PatchMapping("/read/{notificationHistoriesId}")
    public ResponseEntity<Void> markAsRead(@PathVariable Long notificationHistoriesId) {
        notificationService.markAsRead(notificationHistoriesId);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "알림 전체 읽음 처리",
            description = "사용자의 모든 알림을 읽음 처리합니다.",
            parameters = {
                    @Parameter(name = "userId", in = ParameterIn.PATH, required = true,
                            description = "사용자 ID", example = "1")
            }
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "전체 읽음 처리 성공"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PatchMapping("/read-all/{userId}")
    public ResponseEntity<Void> markAllAsRead(@PathVariable Long userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "알림 단건 삭제",
            description = "사용자의 특정 알림 이력을 삭제합니다.",
            parameters = {
                    @Parameter(name = "userId", in = ParameterIn.PATH, required = true,
                            description = "사용자 ID", example = "1"),
                    @Parameter(name = "notificationHistoriesId", in = ParameterIn.PATH, required = true,
                            description = "알림 이력 ID", example = "2")
            }
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "404", description = "대상 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @DeleteMapping("/delete/{userId}/{notificationHistoriesId}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long userId, @PathVariable Long notificationHistoriesId) {
        notificationService.deleteNotificationHistory(userId, notificationHistoriesId);
        return ResponseEntity.ok().build();
    }
}