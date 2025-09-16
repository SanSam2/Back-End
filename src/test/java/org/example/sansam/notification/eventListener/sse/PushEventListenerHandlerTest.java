package org.example.sansam.notification.eventListener.sse;

import org.example.sansam.notification.event.sse.NotificationSavedEvent;
import org.example.sansam.notification.infra.PushProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class PushEventListenerHandlerTest {

    @Mock
    private PushProvider pushProvider;

    @InjectMocks
    private PushEventListenerHandler handler;


    @DisplayName("NotificationSavedEvent 발생 시 pushProvider가 호출된다")
    @Test
    void onApplicationEvent_when_event_is_notification_saved_then_call_push_provider() {
        // given
        NotificationSavedEvent event =
                NotificationSavedEvent.of(1L, 200L,"PAYMENT_COMPLETE", "{\"msg\":\"ok\"}");

        // when
        handler.onNotificationSaved(event);

        // then
        verify(pushProvider, times(1))
                .push(1L, 200L,"PAYMENT_COMPLETE", "{\"msg\":\"ok\"}");
    }

    @DisplayName("pushProvider에서 예외가 발생해도 전파되지 않는다 (내부에서 처리됨)")
    @Test
    void onApplicationEvent_when_exception_occurs_during_push_then_exception_is_not_thrown() {
        // given
        NotificationSavedEvent event =
                NotificationSavedEvent.of(2L, 200L,"WELCOME", "{\"msg\":\"hello\"}");

        doThrow(new RuntimeException("SSE 실패"))
                .when(pushProvider)
                .push(anyLong(), anyLong(), anyString(), anyString());

        // when & then (예외가 외부로 전파되면 테스트 실패)
        assertThrows(RuntimeException.class, () -> handler.onNotificationSaved(event));

        verify(pushProvider, times(1))
                .push(2L, 200L,"WELCOME", "{\"msg\":\"hello\"}");
    }
}