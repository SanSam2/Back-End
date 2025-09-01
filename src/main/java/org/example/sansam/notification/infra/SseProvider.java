package org.example.sansam.notification.infra;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile({"local","prod","test"})
public class SseProvider implements PushProvider {

    private final SseConnector sseConnector;

    @Override
    public void push(Long userId, String eventName, String payloadJson) {
        List<SseEmitter> emitters = sseConnector.getEmitters(userId);

        if (emitters.isEmpty()) return;

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventName)
                        .data(payloadJson));
            } catch (Exception e) {
                handleSendFailure(userId, eventName, emitter, e);
            }
        }
    }

    // TODO : 만약에 sse로 payload 던져주는데 emitter 유실. 그러면 db에 저장만되고 실시간으로 날라가진 않음. retriable?을 생각.
    @Override
    public void broadcast(String eventName, String payloadJson) {
        Map<Long, List<SseEmitter>> allEmitters = sseConnector.getAllEmitters();

        for (Map.Entry<Long, List<SseEmitter>> entry : allEmitters.entrySet()) {
            Long userId = entry.getKey();
            List<SseEmitter> emitters = entry.getValue();
            if (emitters == null || emitters.isEmpty()) {
                continue;
            }

            for (SseEmitter emitter : emitters) {
                try {
                    emitter.send(SseEmitter.event()
                            .name(eventName)
                            .data(payloadJson));
                } catch (Exception e) {
                    handleSendFailure(userId, eventName, emitter, e);
                }
            }
        }
    }

    private void handleSendFailure(Long userId, String eventName, SseEmitter emitter, Exception e) {
        if (e instanceof IOException && e.getMessage() != null && e.getMessage().contains("Broken pipe")) {
            log.debug("클라이언트 연결 종료됨 (정상) userId={}, event={}", userId, eventName);
        } else {
            log.error("SSE 전송 실패 userId={}, event={}", userId, eventName, e);
        }
        emitter.completeWithError(e);
        sseConnector.removeEmitter(userId, emitter);
    }
}
