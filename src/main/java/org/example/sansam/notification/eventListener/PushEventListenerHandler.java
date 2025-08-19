package org.example.sansam.notification.eventListener;

import lombok.RequiredArgsConstructor;
import org.example.sansam.notification.event.NotificationSavedEvent;
import org.example.sansam.notification.infra.PushProvider;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class PushEventListenerHandler {

    private final PushProvider pushProvider;

    @Async("pushExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onNotificationSaved(NotificationSavedEvent event){
        pushProvider.push(event.getUserId(), event.getEventName(), event.getPayloadJson());
    }
}
