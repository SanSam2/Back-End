package org.example.sansam.notification.infra;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;
import static org.awaitility.Awaitility.await;

@SpringBootTest(classes = SseConnector.class)
@ActiveProfiles("test")
class SseConnectorTest {

    @Autowired
    private SseConnector sseConnector;


    @DisplayName("같은 userId로 여러번 connect하면 emitter 리스트에 쌓인다.")
    @Test
    void connect() {
        // given
        Long userId = 1L;
        // when
        SseEmitter first = sseConnector.connect(userId);
        SseEmitter second = sseConnector.connect(userId);
        // then
        assertThat(sseConnector.getEmitters(userId))
                .contains(first, second);

        assertThat(first).isNotSameAs(second);
    }

    @DisplayName("userId가 null이거나 0이면 IllegalArgumentException을 던진다.")
    @Test
    void connect_when_userId_is_invalid_then_throw_exception() {
        // given
        // when
        // then
        Assertions.assertThrows(IllegalArgumentException.class, () -> sseConnector.connect(null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> sseConnector.connect(0L));
        Assertions.assertThrows(IllegalArgumentException.class, () -> sseConnector.connect(-5L));
    }

    @DisplayName("getAllEmitters는 모든 유저의 emitter를 반환한다.")
    @Test
    void getAllEmitters() {
        // given
        Long userId = 1L;
        Long userId2 = 2L;

        SseEmitter first = sseConnector.connect(userId);
        SseEmitter second = sseConnector.connect(userId2);
        // when
        Map<Long, List<SseEmitter>> allEmitters = sseConnector.getAllEmitters();

        // then
        assertThat(allEmitters).containsKeys(userId, userId2);
        assertThat(allEmitters.get(userId)).containsExactly(first);
        assertThat(allEmitters.get(userId2)).containsExactly(second);
    }

    //complete(), timeout(), error() 같은 콜백 동작은
    //Spring MVC 내부가 실행해줘야 하므로 단위 테스트에서는 절대 제대로 검증할 수 없습니다.
    //(= 테스트 코드에서 emitter.complete() 호출해도 callback 이 안 돌죠)

    @DisplayName("removeEmitter - emitter를 제거하면 해당 user의 emitter 수가 줄어든다.")
    @Test
    void removeEmitter_removes_one_emitter() {
        // given
        SseConnector connector = new SseConnector();
        Long userId = 1L;
        SseEmitter emitter1 = connector.connect(userId);
        SseEmitter emitter2 = connector.connect(userId);

        assertThat(connector.getEmitters(userId)).hasSize(2);

        // when
        connector.removeEmitter(emitter1);

        // then
        List<SseEmitter> remaining = connector.getEmitters(userId);
        assertThat(remaining).containsExactly(emitter2);
        assertThat(connector.getAllEmitters()).containsKey(userId);
    }

    @DisplayName("removeEmitter - 마지막 emitter 제거 시 userId 키가 Map에서 삭제된다.")
    @Test
    void removeEmitter_removes_last_emitter_and_cleans_map() {
        // given
        SseConnector connector = new SseConnector();
        Long userId = 2L;
        SseEmitter emitter = connector.connect(userId);

        assertThat(connector.getEmitters(userId)).hasSize(1);

        // when
        connector.removeEmitter(emitter);

        // then
        assertThat(connector.getEmitters(userId)).isEmpty();
        assertThat(connector.getAllEmitters()).doesNotContainKey(userId);
    }

}