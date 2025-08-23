package org.example.sansam.notification.controller;

import org.example.sansam.notification.infra.PushConnector;
import org.example.sansam.notification.infra.SseConnector;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.yaml.snakeyaml.emitter.EmitterException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@WebMvcTest(SseController.class)
@AutoConfigureMockMvc()
class SseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PushConnector pushConnector;

    @DisplayName("구독 성공 시 200과 text/event/stream 반환")
    @Test
    void subscribe_ok() throws Exception {
        // given
        Long userId = 1L;

        // 테스트가 끝날 때까지 살아있을 emitter 준비
        SseEmitter emitter = new SseEmitter(1_000L);

        // when
        when(pushConnector.connect(userId)).thenReturn(emitter);

        // then
        mockMvc.perform(get("/api/notifications/subscribe/{userId}", userId)
                        .accept(MediaType.TEXT_EVENT_STREAM))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.request().asyncStarted());

        verify(pushConnector, times(1)).connect(userId);

        emitter.complete();
    }

    @DisplayName("SSE 연결실패 EmitterException 이면 503을 반환한다.")
    @Test
    void subscribe_fail_with_emitter_exception() throws Exception {
        // given
        Long userId = 1L;

        // when
        when(pushConnector.connect(userId)).thenThrow(new EmitterException("boom"));

        // then

        mockMvc.perform(get("/api/notifications/subscribe/{userId}", userId)
                .accept(MediaType.TEXT_EVENT_STREAM))
                .andExpect(status().isServiceUnavailable());

        verify(pushConnector, times(1)).connect(userId);
    }

    @DisplayName("기타 예외 발생 시 500 반환")
    @Test
    void subscribe_fail_with_other_exception() throws Exception {
        // given
        Long userId = 1L;
        // when
        when(pushConnector.connect(userId)).thenThrow(new IllegalArgumentException("boom"));

        // then
        mockMvc.perform(get("/api/notifications/subscribe/{userId}", userId)
                .accept(MediaType.TEXT_EVENT_STREAM))
                .andExpect(status().isInternalServerError());

        verify(pushConnector, times(1)).connect(userId);
    }
}