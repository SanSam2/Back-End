package org.example.sansam.chat.config;

import lombok.RequiredArgsConstructor;
import org.example.sansam.chat.service.ChatMemberService;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.time.LocalDateTime;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final ChatMemberService chatMemberService;

    @EventListener
    public void onDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        Map<String,Object> attrs = sha.getSessionAttributes();
        if (attrs == null) return;

        Long userId = (Long) attrs.get("userId"); // HTTP 세션에서 복사된 값
        Long roomId = (Long) attrs.get("roomId"); // 방 구독 시에 저장된 값
        if (userId != null && roomId != null) {
            chatMemberService.updateLastReadAt(userId, roomId, LocalDateTime.now());
        }
    }

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        System.out.println("[WebSocket] 클라이언트 연결 시도: sessionId=" + sha.getSessionId());
    }

    @EventListener
    public void handleWebSocketConnectedListener(SessionConnectedEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        System.out.println("[WebSocket] 클라이언트 연결 완료: sessionId=" + sha.getSessionId());
    }
}