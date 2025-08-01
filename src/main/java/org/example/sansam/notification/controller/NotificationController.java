package org.example.sansam.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.sansam.notification.service.NotificationService;
import org.example.sansam.user.domain.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.yaml.snakeyaml.emitter.EmitterException;

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

    @GetMapping("/subscribe")
    public ResponseEntity<SseEmitter> subscribe(@RequestBody User user) {
        try {
            SseEmitter emitter = notificationService.connect(user.getId());
            return ResponseEntity.ok(emitter);
        } catch (EmitterException e) {
            // 커스텀 예외로 명확하게 알 수 있게
            log.error("SSE 연결 실패 - userId: {}", user.getId(), e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        } catch (Exception e) {
            log.error("알 수 없는 SSE 연결 오류 - userId: {}", user.getId(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


}
