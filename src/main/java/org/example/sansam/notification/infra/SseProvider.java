package org.example.sansam.notification.infra;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile({"local", "test"})
public class SseProvider implements PushProvider {

    private final SseConnector sseConnector;

    @Override
    public void push(Long userId, String eventName, String payloadJson) {
        List<SseEmitter> emitters = sseConnector.getEmitters(userId);

        if (emitters == null || emitters.isEmpty()) {
            return;
        }

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventName)
                        .data(payloadJson), MediaType.APPLICATION_JSON);
            } catch (Exception e) {
                log.warn("SSE 전송 실패 userId={}, event={}", userId, eventName, e);
                emitter.completeWithError(e);
                sseConnector.getEmitters(userId).remove(emitter);
            }
        }
    }
    // TODO : 지금 push 같은 경우에는 asyncconfig에 @Async 붙어있어서 비동기 보장돼 있지만 broadcast 같은 경우엔 미적용.
    //  그리고 이걸 어떤 걸로 보낼지, event, eventListener 만들어서 이벤트 발행 시키고, 여기까지 타고 오게끔 구현해야함.
    @Override
    public void broadcast(String eventName, String payloadJson) {
        Map<Long, List<SseEmitter>> allEmitters = sseConnector.getAllEmitters();

        for (Map.Entry<Long, List<SseEmitter>> entry : allEmitters.entrySet()) {

            List<SseEmitter> emitters = entry.getValue();
            for (SseEmitter emitter : emitters) {
                try {
                    emitter.send(SseEmitter.event()
                            .name(eventName)
                            .data(payloadJson), MediaType.APPLICATION_JSON);
                } catch (Exception e) {
                    log.warn("SSE 전송 실패 userId={}, event={}", entry.getKey(), eventName, e);
                    emitter.completeWithError(e);
                    sseConnector.getAllEmitters().get(entry.getKey()).remove(emitter);
                }
            }
        }
    }

}
