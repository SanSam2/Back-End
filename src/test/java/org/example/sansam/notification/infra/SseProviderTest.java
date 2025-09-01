package org.example.sansam.notification.infra;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.example.sansam.notification.domain.NotificationHistories;
import org.example.sansam.notification.dto.NotificationDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class SseProviderTest {


    @DisplayName("emitter가 없으면 send를 호출하지 않는다.")
    @Test
    void emit_when_emitter_is_not_exists_then_not_call_send_method() {
        // given
        SseConnector sseConnector = mock(SseConnector.class);
        Executor executor = Runnable::run;
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        when(sseConnector.getEmitters(1L)).thenReturn(List.of());

        SseProvider sseProvider = new SseProvider(sseConnector, executor, meterRegistry);
        // when
        sseProvider.push(1L, 200L, "welcomeMessage", "{\"msg\":\"hi\"}");
        // then
        // send를 아예 호출 안 하니까 verify 불필요, 단지 exception만 안 터지면 OK
        verify(sseConnector, times(1)).getEmitters(1L);
    }

    @DisplayName("emitter가 있으면 send를 호출한다.")
    @Test
    void emit_when_emitter_is_exists_then_call_send_method() throws IOException {
        // given
        SseConnector sseConnector = mock(SseConnector.class);
        Executor executor = Runnable::run;
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        SseEmitter emitter = mock(SseEmitter.class);

        when(sseConnector.getEmitters(1L)).thenReturn(new java.util.ArrayList<>(List.of(emitter)));

        SseProvider sseProvider = new SseProvider(sseConnector, executor, meterRegistry);
        // when
        sseProvider.push(1L, 200L, "welcomeMessage", "{\"msg\":\"hi\"}");

        // then
        verify(emitter, times(1)).send(
                any(SseEmitter.SseEventBuilder.class)
        );
    }

    @DisplayName("send중 예외가 발생하면 completeWithError를 호출한다.")
    @Test
    void push_when_send_throws_exception_then_call_completeWithError_method() throws IOException {
        // given
        SseConnector sseConnector = mock(SseConnector.class);
        SseEmitter emitter = mock(SseEmitter.class);
        Executor executor = Runnable::run;
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();

        when(sseConnector.getEmitters(1L)).thenReturn(new java.util.ArrayList<>(List.of(emitter)));
        doThrow(new RuntimeException("boom"))
                .when(emitter).send(any(SseEmitter.SseEventBuilder.class));

        SseProvider sseProvider = new SseProvider(sseConnector, executor, meterRegistry);
        // when
        sseProvider.push(1L, 200L, "welcomeMessage", "{\"msg\":\"hi\"}");

        // then
        verify(emitter, times(1)).completeWithError(any(RuntimeException.class));
    }

    @DisplayName("broadcast는 모든 유저의 emitter의 send를 호출한다.")
    @Test
    void broadcast_when_all_users_have_emitters_then_call_send_method_of_all_emitters() throws IOException {
        // given
        SseConnector sseConnector = mock(SseConnector.class);
        SseEmitter emitter1 = mock(SseEmitter.class);
        SseEmitter emitter2 = mock(SseEmitter.class);

        when(sseConnector.getAllEmitters()).thenReturn(
                Map.of(
                        1L, new CopyOnWriteArrayList<>(List.of(emitter1)),
                        2L, new CopyOnWriteArrayList<>(List.of(emitter2))
                )
        );

        SseProvider sseProvider = new SseProvider(sseConnector);
        // when

        sseProvider.broadcast("welcomeMessage", "{\"msg\":\"hi\"}");
        // then
        verify(emitter1, times(1)).send(any(SseEmitter.SseEventBuilder.class));
        verify(emitter2, times(1)).send(any(SseEmitter.SseEventBuilder.class));

    }

    @DisplayName("broadcast 중 send 예외 발생 시 completeWithError를 호출하고 emitter를 제거한다.")
    @Test
    void broadcast_when_send_throws_exception_then_call_completeWithError_method_and_remove_emitter() throws IOException {
        // given
        SseConnector sseConnector = mock(SseConnector.class);
        SseEmitter badEmitter = mock(SseEmitter.class);


        doThrow(new RuntimeException("boom"))
                .when(badEmitter).send(any(SseEmitter.SseEventBuilder.class));

        when(sseConnector.getAllEmitters()).thenReturn(Map.of(1L, List.of(badEmitter)));

        SseProvider sseProvider = new SseProvider(sseConnector);

        // when
        sseProvider.broadcast("welcomeMessage", "{\"msg\":\"hi\"}");

        // then
        verify(badEmitter, times(1)).completeWithError(any(RuntimeException.class));
        verify(sseConnector, times(1)).removeEmitter(1L, badEmitter);
    }

    @DisplayName("broadcast 중 Broken pipe IOException 발생 시 debug 로그를 찍고 emitter를 제거한다.")
    @Test
    void broadcast_when_broken_pipe_exception_occurs_then_log_and_remove_emitter() throws IOException {
        // given
        SseConnector sseConnector = mock(SseConnector.class);
        SseEmitter badEmitter = mock(SseEmitter.class);

        doThrow(new IOException("Broken pipe"))
                .when(badEmitter).send(any(SseEmitter.SseEventBuilder.class));

        when(sseConnector.getAllEmitters()).thenReturn(Map.of(1L, List.of(badEmitter)));
        SseProvider sseProvider = new SseProvider(sseConnector);

        // when
        sseProvider.broadcast("welcomeMessage", "{\"msg\":\"Broken pipe test\"}");

        // then
        verify(badEmitter, times(1)).completeWithError(any(IOException.class));
        verify(sseConnector, times(1)).removeEmitter(1L, badEmitter);
    }
}