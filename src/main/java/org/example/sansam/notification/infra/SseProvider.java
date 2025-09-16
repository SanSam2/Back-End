package org.example.sansam.notification.infra;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.sansam.notification.domain.NotificationHistories;
import org.example.sansam.notification.dto.NotificationDTO;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile({"local", "prod", "test"})
public class SseProvider implements PushProvider {

    private final SseConnector sseConnector;
    private final Executor virtualBroadcastExecutor;
    private final MeterRegistry meterRegistry;
    private Counter sendErrorCounter;
    private Timer broadcastTimer;
    private static final int BATCH_SIZE = 200;
    private static final int SUB_BATCH_SIZE = 50;

    @PostConstruct
    public void init() {
        sendErrorCounter = Counter.builder("sse_broadcast_errors")
                .description("Number of fail SSE sends")
                .register(meterRegistry);

        broadcastTimer = Timer.builder("sse_broadcast_duration")
                .description("Broadcast push latency")
                .publishPercentiles(0.95)   // Prometheus에서 p95 바로 쓸 수 있음
                .register(meterRegistry);
    }

    @Override
    public void push(Long userId, Long nhId, String eventName, String payloadJson) {
        List<SseEmitter> emitters = sseConnector.getEmitters(userId);

        if (emitters.isEmpty()) return;

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .id(String.valueOf(nhId))
                        .name(eventName)
                        .data(payloadJson));
            } catch (Exception e) {
                handleSendFailure(eventName, emitter, e);
            }
        }
    }

    @Override
    public void broadcast(Long nhId, String eventName, String payloadJson) {
        Map<Long, List<SseEmitter>> allEmitters = sseConnector.getAllEmitters();
        List<SseEmitter> flatList = allEmitters.values().stream()
                .flatMap(List::stream)
                .toList();

        log.info(">>> Broadcast 전송 시작 - nhId={}, eventName={}, 전체 emitter 수={}", nhId, eventName, flatList.size());

        int batchCount = 0;

        for (int i = 0; i < flatList.size(); i += BATCH_SIZE) {
            int batchId = batchCount++;
            int end = Math.min(i + BATCH_SIZE, flatList.size());
            List<SseEmitter> batch = flatList.subList(i, end);

            // batch 자체도 fire-and-forget
            CompletableFuture.runAsync(() -> {
                long batchStart = System.currentTimeMillis();
                int subBatchCount = 0;

                for (int j = 0; j < batch.size(); j += SUB_BATCH_SIZE) {
                    int subBatchId = subBatchCount++;
                    int subEnd = Math.min(j + SUB_BATCH_SIZE, batch.size());
                    List<SseEmitter> subBatch = batch.subList(j, subEnd);

                    // subBatch도 fire-and-forget (join 없음)
                    CompletableFuture.runAsync(() -> {
                        long start = System.currentTimeMillis();
                        int success = 0, fail = 0;

                        for (SseEmitter emitter : subBatch) {
                            try {
                                emitter.send(SseEmitter.event()
                                        .id(String.valueOf(nhId))
                                        .name(eventName)
                                        .data(payloadJson));
                                success++;
                            } catch (Exception e) {
                                fail++;
                                handleSendFailure(eventName, emitter, e);
                            }
                        }

                        long elapsed = System.currentTimeMillis() - start;
                        broadcastTimer.record(elapsed, TimeUnit.MILLISECONDS);

                        log.info("  ▶ SubBatch-{} 실행 완료 (size={}, success={}, fail={}, latency={}ms)",
                                subBatchId, subBatch.size(), success, fail, elapsed);
                    }, virtualBroadcastExecutor);
                }

                long enqueueElapsed = System.currentTimeMillis() - batchStart;
                log.info("▶ Batch-{} 전송 요청 완료 (subBatch 수={}, size={}, enqueueElapsed={}ms)",
                        batchId, subBatchCount, batch.size(), enqueueElapsed);
            }, virtualBroadcastExecutor);
        }

        log.info(">>> Broadcast 전체 전송 요청 완료 - batch 수={}, 총 emitter={}", batchCount, flatList.size());
    }



    public void resend(Long userId, List<NotificationHistories> missed) {
        if (missed == null || missed.isEmpty()) return;

        for (NotificationHistories nh : missed) {
            try {
                String payload = NotificationDTO.from(nh).toJson();
                String eventName = nh.getEventName();
                push(userId, nh.getId(), eventName, payload);
            } catch (Exception e) {
                log.error("재전송 실패 userId = {}, nhId = {}", userId, nh.getId(), e);
            }
        }
    }

    private void handleSendFailure(String eventName, SseEmitter emitter, Exception e) {
        boolean isNormalClose =
                (e instanceof IOException && e.getMessage() != null && e.getMessage().contains("Broken pipe")) ||
                        (e instanceof IllegalStateException && e.getMessage() != null && e.getMessage().contains("completed"));

        if (isNormalClose) {
            log.debug("클라이언트 연결 종료됨 (정상), event={}", eventName);
        } else {
            sendErrorCounter.increment();
            log.error("SSE 전송 실패, event={}", eventName, e);
            emitter.completeWithError(e);
        }

        sseConnector.removeEmitter(emitter);
    }
}
