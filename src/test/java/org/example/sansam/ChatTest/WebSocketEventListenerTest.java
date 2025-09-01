package org.example.sansam.ChatTest;

import org.example.sansam.chat.config.WebSocketEventListener;
import org.example.sansam.chat.service.ChatMemberService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebSocketEventListenerTest {

    @Mock
    ChatMemberService chatMemberService;

    @InjectMocks
    WebSocketEventListener listener;

    @Captor
    ArgumentCaptor<LocalDateTime> tsCaptor;

    @Test
    @DisplayName("Disconnect 이벤트 시, 세션에 userId와 roomId가 있으면 lastReadAt 업데이트 호출")
    void onDisconnect_shouldCallUpdateLastReadAt_whenSessionAttrsContainUserAndRoom() {
        StompHeaderAccessor sha = StompHeaderAccessor.create(StompCommand.DISCONNECT);

        // ❷ 반드시 sessionId 를 세팅해 줘야 SessionDisconnectEvent 생성 시 에러가 안 납니다.
        sha.setSessionId("test-session-1");

        // ❸ 세션 속성에 userId, roomId 담기
        Map<String,Object> attrs = new HashMap<>();
        attrs.put("userId", 42L);
        attrs.put("roomId", 99L);
        sha.setSessionAttributes(attrs);

        // ❹ Message 에 헤더 붙이기
        Message<byte[]> message =
                MessageBuilder.createMessage(new byte[0], sha.getMessageHeaders());

        // ❺ SessionDisconnectEvent 생성 (sessionId 가 null 이 아니어야 함)
        SessionDisconnectEvent event =
                new SessionDisconnectEvent(this, message, sha.getSessionId(), null);

        // ❻ 리스너 호출
        listener.onDisconnect(event);

        // ❼ 서비스 호출 검증
        verify(chatMemberService, times(1))
                .updateLastReadAt(eq(42L), eq(99L), tsCaptor.capture());

        // 타임스탬프가 “지금” 근처인지 확인
        LocalDateTime calledTs = tsCaptor.getValue();
        assertThat(calledTs)
                .isBetween(LocalDateTime.now().minusSeconds(5),
                        LocalDateTime.now().plusSeconds(5));
    }

    @Test
    @DisplayName("Disconnect 이벤트 시, 세션 속성이 없으면 서비스 호출 없음")
    void onDisconnect_shouldDoNothing_whenNoSessionAttrs() {
        // ❶ StompHeaderAccessor 생성 & sessionId 세팅
        StompHeaderAccessor sha = StompHeaderAccessor.create(StompCommand.DISCONNECT);
        sha.setSessionId("test-session-2");

        // ❷ 세션 속성 자체를 null 로
        sha.setSessionAttributes(null);

        Message<byte[]> message =
                MessageBuilder.createMessage(new byte[0], sha.getMessageHeaders());
        SessionDisconnectEvent event =
                new SessionDisconnectEvent(this, message, sha.getSessionId(), null);

        listener.onDisconnect(event);

        // 서비스가 전혀 호출되지 않아야 함
        verifyNoInteractions(chatMemberService);
    }

    @Test
    @DisplayName("Disconnect 이벤트 시, 세션 속성이 존재하면 서비스 호출됨")
    void onDisconnect_shouldCallService_whenSessionAttrsExist() {
        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put("userId", 1L);
        sessionAttrs.put("roomId", 10L);

        StompHeaderAccessor sha = StompHeaderAccessor.create(StompCommand.DISCONNECT);
        sha.setSessionId("test-session-1");
        sha.setSessionAttributes(sessionAttrs);

        Message<byte[]> message =
                MessageBuilder.createMessage(new byte[0], sha.getMessageHeaders());
        SessionDisconnectEvent event =
                new SessionDisconnectEvent(this, message, sha.getSessionId(), null);

        listener.onDisconnect(event);

        verify(chatMemberService).updateLastReadAt(eq(1L), eq(10L), any());
    }
}
