package org.example.sansam.notification.eventListener.sse;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.sansam.notification.event.sse.BroadcastEvent;
import org.example.sansam.notification.infra.PushProvider;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Log4j2
@Component
@RequiredArgsConstructor
public class BroadCastEventListener {
    private final PushProvider pushProvider;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleBroadcastEvent(BroadcastEvent event) {
        try {
            pushProvider.broadcast(event.getNhId(), event.getEventName(), event.getPayloadJson());
        } catch (Exception e) {
            log.error("broadcast 실패 - eventName={}, payloadJson={}", event.getEventName(), event.getPayloadJson(), e);
        }
    }
}
