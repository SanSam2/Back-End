package org.example.sansam.notification.infra;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class SseConnector implements PushConnector {

    private final Map<Long, List<SseEmitter>> sseEmitters = new ConcurrentHashMap<>();
    private static final long DEFAULT_TIMEOUT = 60L * 1000 * 30; // 30분 설정, 리소스 점유 시간 절감

    @Override
    public SseEmitter connect(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("Invalid userId: " + userId);
        }

        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
        sseEmitters.computeIfAbsent(userId, key -> new CopyOnWriteArrayList<>())
                .add(emitter);

        // 연결 종료 시 emitter 제거
        emitter.onCompletion(() -> sseEmitters.remove(userId));
        emitter.onTimeout(() -> sseEmitters.remove(userId));
        emitter.onError(ex -> sseEmitters.remove(userId));

        return emitter;
    }

    public List<SseEmitter> getEmitters(Long userId) {
        return sseEmitters.getOrDefault(userId, List.of());
    }

    public Map<Long, List<SseEmitter>> getAllEmitters() {
        return sseEmitters;
    }
}
