package org.example.sansam.notification.eventListener.sse;

import org.example.sansam.notification.event.sse.BroadcastEvent;
import org.example.sansam.notification.infra.PushProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class BroadCastEventListenerTest {

    @Mock
    private PushProvider pushProvider;

    @InjectMocks
    private BroadCastEventListener broadCastEventListener;

    @DisplayName("BroadcastEvent가 발생하면 pushProvider.broadcast가 호출된다.")
    @Test
    void handleBroadCastEvent_when_event_is_broadcast_then_call_push_provider_broadcast() {
        // given
        String eventName = "broadcastMessage";
        String payloadJson = "{\"msg\":\"hello\"}";
        BroadcastEvent event = new BroadcastEvent(200L, eventName, payloadJson);
        // when
        broadCastEventListener.handleBroadcastEvent(event);

        // then
        verify(pushProvider, times(1)).broadcast(200L, eventName, payloadJson);
    }

    @DisplayName("pushProvider.broadcast에서 예외가 발생해도 에러 로그를 남기고 예외를 전파하지 않는다.")
    @Test
    void handleBroadCastEvent_when_exception_occurs_during_push_then_exception_is_not_thrown() {
        // given
        String eventName = "broadcastMessage";
        String payloadJson = "{\"msg\":\"hello\"}";
        BroadcastEvent event = new BroadcastEvent(200L, eventName, payloadJson);

        doThrow(new RuntimeException("push 실패"))
                .when(pushProvider).broadcast(200L, eventName, payloadJson);

        // when
        // then

        broadCastEventListener.handleBroadcastEvent(event);

        verify(pushProvider, times(1)).broadcast(200L, eventName, payloadJson);
    }
}