package org.example.sansam.notification.infra;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SseConnector implements PushConnector{

    private final Map<Long, SseEmitter> sseEmitters = new ConcurrentHashMap<>();
    private static final long DEFAULT_TIMEOUT = 60L * 1000 * 30; // 30분 설정, 리소스 점유 시간 절감

    @Override
    public SseEmitter connect(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("Invalid userId: " + userId);
        }

        SseEmitter existingEmitter = sseEmitters.get(userId);
        if (existingEmitter != null) {
            existingEmitter.complete(); // 이전 연결 정리
            sseEmitters.remove(userId);
        }

        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
        sseEmitters.put(userId, emitter);

        // 연결 종료 시 emitter 제거
        emitter.onCompletion(() -> sseEmitters.remove(userId));
        emitter.onTimeout(() -> sseEmitters.remove(userId));
        emitter.onError(ex -> sseEmitters.remove(userId));

        return emitter;
    }

    public SseEmitter getEmitter(Long userId) {
        return sseEmitters.get(userId);
    }
}
