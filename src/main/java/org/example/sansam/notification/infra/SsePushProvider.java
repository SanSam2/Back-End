package org.example.sansam.notification.infra;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Component
@RequiredArgsConstructor
@Profile({"local","test"})
public class SsePushProvider implements PushProvider{

    private final SseConnector sseConnector;

    @Override
    public void push(Long userId, String eventName, String payloadJson) {
        SseEmitter emitter = sseConnector.getEmitter(userId);

        if (emitter == null) return;

        try {
            emitter.send(SseEmitter.event()
                    .name(eventName)
                    .data(payloadJson), MediaType.APPLICATION_JSON);
        }catch (Exception e){
            emitter.completeWithError(e);
        }
    }
}
