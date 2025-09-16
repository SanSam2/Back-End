package org.example.sansam.notification.infra;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
@Log4j2
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

        int userEmitterCount = sseEmitters.get(userId).size();
        int totalEmitterCount = getTotalEmitterCount();

        log.info("✅ Emitter 생성 - userId={}, 현재 user별 emitter 수={}, 전체 emitter 수={}",
                userId, userEmitterCount, totalEmitterCount);

        // 연결 종료 시 emitter 제거
        emitter.onCompletion(() -> removeEmitter(userId, emitter));
        emitter.onTimeout(() -> removeEmitter(userId, emitter));
        emitter.onError(ex -> removeEmitter(userId, emitter));

        return emitter;
    }

    public List<SseEmitter> getEmitters(Long userId) {
        return sseEmitters.getOrDefault(userId, List.of());
    }

    public Map<Long, List<SseEmitter>> getAllEmitters() {
        return sseEmitters;
    }

    public void removeEmitter(Long userId, SseEmitter emitter) {
        List<SseEmitter> emitters = sseEmitters.get(userId);
        if (emitters != null) {
            emitters.remove(emitter);
            int userEmitterCount = emitters.size();
            int totalEmitterCount = getTotalEmitterCount();

            log.info("❌ Emitter 제거 - userId={}, 남은 user별 emitter 수={}, 전체 emitter 수={}",
                    userId, userEmitterCount, totalEmitterCount);
            if (emitters.isEmpty()) {
                sseEmitters.remove(userId); // 메모리 정리
            }
        }
    }

    private int getTotalEmitterCount() {
        return sseEmitters.values().stream()
                .mapToInt(List::size)
                .sum();
    }
}
