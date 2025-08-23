package org.example.sansam.notification.infra;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

import static org.mockito.Mockito.*;

@ActiveProfiles("test")
class SseProviderTest {


    @DisplayName("emitter가 없으면 send를 호출하지 않는다.")
    @Test
    void emit_when_emitter_is_not_exists_then_not_call_send_method() {
        // given
        SseConnector sseConnector = mock(SseConnector.class);
        when(sseConnector.getEmitter(1L)).thenReturn(null);

        SseProvider sseProvider = new SseProvider(sseConnector);
        // when
        sseProvider.push(1L,"welcomeMessage","{\"msg\":\"hi\"}");
        // then
        // send를 아예 호출 안 하니까 verify 불필요, 단지 exception만 안 터지면 OK
        verify(sseConnector,times(1)).getEmitter(1L);
    }

    @DisplayName("emitter가 있으면 send를 호출한다.")
    @Test
    void emit_when_emitter_is_exists_then_call_send_method() throws IOException {
        // given
        SseConnector sseConnector = mock(SseConnector.class);
        SseEmitter emitter = mock(SseEmitter.class);

        when(sseConnector.getEmitter(1L)).thenReturn(emitter);

        SseProvider sseProvider = new SseProvider(sseConnector);
        // when
        sseProvider.push(1L,"welcomeMessage","{\"msg\":\"hi\"}");

        // then
        verify(emitter, times(1)).send(
                any(SseEmitter.SseEventBuilder.class),
                eq(MediaType.APPLICATION_JSON)
        );
    }

    @DisplayName("send중 예외가 발생하면 completeWithError를 호출한다.")
    @Test
    void push_when_send_throws_exception_then_call_completeWithError_method() throws IOException {
        // given
        SseConnector sseConnector = mock(SseConnector.class);
        SseEmitter emitter = mock(SseEmitter.class);

        when(sseConnector.getEmitter(1L)).thenReturn(emitter);
        doThrow(new RuntimeException("boom"))
                .when(emitter).send(any(SseEmitter.SseEventBuilder.class),
                        eq(MediaType.APPLICATION_JSON));

        SseProvider sseProvider = new SseProvider(sseConnector);
        // when
        sseProvider.push(1L,"welcomeMessage","{\"msg\":\"hi\"}");

        // then
        verify(emitter, times(1)).completeWithError(any(RuntimeException.class));
    }
}