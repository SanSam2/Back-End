package org.example.sansam.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.sansam.notification.dto.NotificationDTO;
import org.example.sansam.notification.exception.CustomException;
import org.example.sansam.notification.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.yaml.snakeyaml.emitter.EmitterException;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
@Log4j2
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "테스트 API", description = "정상/예외 응답 확인",
            responses = {@ApiResponse(responseCode = "200", description = "정상 응답"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "500", description = "서버 에러")})

    @GetMapping(value = "/subscribe/{userId}", produces = "text/event-stream; charset=UTF-8")
    public ResponseEntity<SseEmitter> subscribe(@PathVariable Long userId) {
        try {
            log.info("SSE 구독 요청 - userId: {}", userId);
            SseEmitter emitter = notificationService.connect(userId);
            return ResponseEntity.ok(emitter);

        } catch (EmitterException e) {
            log.error("SSE 연결 실패 - userId: {}", userId, e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();

        } catch (Exception e) {
            log.error("알 수 없는 SSE 연결 오류 - userId: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 로그인 후 알림 기록 가져오기
    @GetMapping("/list/{userId}")
    public ResponseEntity<List<NotificationDTO>> getNotificationList(@PathVariable Long userId) {
        try {
            List<NotificationDTO> histories = notificationService.getNotificationHistories(userId);
            if (histories.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(histories);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 종 모양의 아이콘 위에 읽지 않은 알림 개수 카운트
    @GetMapping("/unread-count/{userId}")
    public ResponseEntity<?> getUnreadNotificationCount(@PathVariable Long userId) {
        try {
            Long notificationCount = notificationService.getUnreadNotificationCount(userId);

            return ResponseEntity.ok(notificationCount);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("읽지 않은 알림 카운트 실패");
        }
    }

    // 알림 읽으면 isRead = true
    @PatchMapping("/read/{notificationHistoriesId}")
    public ResponseEntity<Void> markAsRead(@PathVariable Long notificationHistoriesId) {
        try {
            notificationService.markAsRead(notificationHistoriesId);

            return ResponseEntity.ok().build();
        } catch (CustomException e) {
            return ResponseEntity.status(e.getErrorCode().getStatus())
                    .build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 알림 모두 읽기 isRead = true
    @PatchMapping("/read-all/{userId}")
    public ResponseEntity<Void> markAllAsRead(@PathVariable Long userId) {
        try {
            notificationService.markAllAsRead(userId);

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 알림 개별 삭제
    @DeleteMapping("/delete/{userId}/{notificationHistoriesId}")
    public  ResponseEntity<Void> deleteNotification(@PathVariable Long userId, @PathVariable Long notificationHistoriesId) {
        try {
            notificationService.deleteNotificationHistory(userId, notificationHistoriesId);
            return ResponseEntity.ok().build();
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}