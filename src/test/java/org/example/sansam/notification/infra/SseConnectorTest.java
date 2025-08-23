package org.example.sansam.notification.infra;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.assertj.core.api.Assertions.*;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(classes = SseConnector.class)
@ActiveProfiles("test")
class SseConnectorTest {

    @Autowired
    private SseConnector sseConnector;


    @DisplayName("사용자는 userId를 넘겼을 때 emitter를 반환받는다.")
    @Test
    void connect() {
        // given
        Long userId = 1L;
        // when
        SseEmitter emitter = sseConnector.connect(userId);
        // then
        Assertions.assertNotNull(emitter, "Emitter는 Null이 아니어야 한다.");
        Assertions.assertSame(emitter, sseConnector.getEmitter(userId), "반환된 emitter는 내부 저장된 emitter와 같아야 한다.");

    }

    @DisplayName("userId가 null이거나 0이면 IllegalArgumentException을 던진다.")
    @Test
    void connect_fail_with_null_or_zero_user_id() {
        // given
        // when
        // then
        Assertions.assertThrows(IllegalArgumentException.class, () -> sseConnector.connect(null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> sseConnector.connect(0L));
        Assertions.assertThrows(IllegalArgumentException.class, () -> sseConnector.connect(-5L));
    }

    @DisplayName("이미 존재하는 emitter가 있으면 제거 후 새 emitter로 대체된다.")
    @Test
    void connect_when_exist_same_user_id_then_remove_and_return_new_emitter() {
        // given
        Long userId = 1L;
        SseEmitter emitter = sseConnector.connect(userId);
        // when
        SseEmitter newEmitter = sseConnector.connect(userId);

        // then

        Assertions.assertNotNull(newEmitter);
        Assertions.assertNotSame(emitter, newEmitter, "새로운 emitter가 기존 emitter와 달라야 함");

        //그리고 기존 emitter는 이미 제거되었기 때문에 다시 저장되어야 함.
        Assertions.assertEquals(newEmitter, sseConnector.getEmitter(userId));
    }

    //complete(), timeout(), error() 같은 콜백 동작은
    //Spring MVC 내부가 실행해줘야 하므로 단위 테스트에서는 절대 제대로 검증할 수 없습니다.
    //(= 테스트 코드에서 emitter.complete() 호출해도 callback 이 안 돌죠)

}