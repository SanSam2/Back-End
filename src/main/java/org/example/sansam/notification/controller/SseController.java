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
import org.example.sansam.notification.infra.PushConnector;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.yaml.snakeyaml.emitter.EmitterException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
@Log4j2
@Tag(name = "Notifications", description = "SSE 구독 API")
public class SseController {

    private final PushConnector pushConnector;

    @Operation(
            summary = "SSE 구독 시작",
            description = """
        클라이언트가 서버와 SSE(EventSource) 연결을 맺습니다.
        응답 스트림은 `text/event-stream`으로 유지됩니다.
        """,
            parameters = {
                    @Parameter(
                            name = "userId", in = ParameterIn.PATH, required = true,
                            description = "구독할 사용자 ID", example = "1"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "SSE 연결 성공",
                    content = @Content(mediaType = "text/event-stream",
                            schema = @Schema(implementation = String.class),
                            examples = @ExampleObject(
                                    name = "EventStream",
                                    value = "id: 1\nevent: notification\ndata: {\"title\":\"결제완료\",\"message\":\"주문이 완료되었습니다.\"}\n\n"
                            )
                    )
            ),
            @ApiResponse(responseCode = "503", description = "SSE 연결 불가(서버 과부하/일시적 오류)"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping(value = "/subscribe/{userId}", produces = "text/event-stream; charset=UTF-8")
    public ResponseEntity<SseEmitter> subscribe(@PathVariable Long userId) {
        try {
            SseEmitter emitter = pushConnector.connect(userId);
            log.info("🔔 SSE 구독 요청 들어옴 - userId={}", userId);
            return ResponseEntity.ok().contentType(MediaType.TEXT_EVENT_STREAM).body(emitter);
        } catch (EmitterException e) {
            log.error("SSE 연결 실패 - userId: {}", userId, e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        } catch (Exception e) {
            log.error("알 수 없는 SSE 연결 오류 - userId: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
