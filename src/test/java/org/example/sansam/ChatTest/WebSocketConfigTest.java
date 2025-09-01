package org.example.sansam.ChatTest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.GenericMessage;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class WebSocketConfigTest {

    // 운영용 인터셉터 (예외 발생 시 로그 출력)
    private ChannelInterceptor createInterceptorWithLogging() {
        return new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> msg, MessageChannel channel) {
                StompHeaderAccessor accessor = StompHeaderAccessor.wrap(msg);

                if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
                    String dest = accessor.getDestination();
                    if (dest != null && dest.startsWith("/sub/chat/room/")) {
                        if (accessor.getSessionAttributes() == null) {
                            accessor.setSessionAttributes(new HashMap<>()); // null 안전 처리
                        }
                        try {
                            Long roomId = Long.valueOf(dest.substring("/sub/chat/room/".length()));
                            accessor.getSessionAttributes().put("roomId", roomId);
                        } catch (NumberFormatException e) {
                            System.out.println("roomId 파싱 실패: " + dest); // 로그 출력
                        }
                    }
                }
                return msg;
            }
        };
    }

    // 테스트용 인터셉터 (예외 그대로 던짐)
    private ChannelInterceptor createInterceptorThrowingException() {
        return new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> msg, MessageChannel channel) {
                StompHeaderAccessor accessor = StompHeaderAccessor.wrap(msg);

                if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
                    String dest = accessor.getDestination();
                    if (dest != null && dest.startsWith("/sub/chat/room/")) {
                        if (accessor.getSessionAttributes() == null) {
                            accessor.setSessionAttributes(new HashMap<>());
                        }
                        // 숫자가 아니면 NumberFormatException 발생
                        Long roomId = Long.valueOf(dest.substring("/sub/chat/room/".length()));
                        accessor.getSessionAttributes().put("roomId", roomId);
                    }
                }
                return msg;
            }
        };
    }

    @Nested
    @DisplayName("preSend 정상 동작 테스트")
    class PreSendNormalTests {

        @Test
        @DisplayName("DEST가 올바른 숫자면 roomId가 세션에 추가되어야 함")
        void validRoomId_shouldPutRoomId() {
            Map<String, Object> sessionAttributes = new HashMap<>();
            StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
            accessor.setDestination("/sub/chat/room/123");
            accessor.setSessionAttributes(sessionAttributes);
            Message<byte[]> message = new GenericMessage<>(new byte[0], accessor.toMessageHeaders());

            createInterceptorWithLogging().preSend(message, null);

            assertThat(sessionAttributes).containsEntry("roomId", 123L);
        }

        @Test
        @DisplayName("DEST가 숫자가 아니면 roomId 추가 안 되고 로그만 출력")
        void destNotNumber_shouldLogAndNotPutRoomId() {
            Map<String, Object> sessionAttributes = new HashMap<>();
            StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
            accessor.setDestination("/sub/chat/room/abc"); // 숫자가 아님
            accessor.setSessionAttributes(sessionAttributes);
            Message<byte[]> message = new GenericMessage<>(new byte[0], accessor.toMessageHeaders());

            createInterceptorWithLogging().preSend(message, null);

            // roomId는 추가되지 않아야 함
            assertThat(sessionAttributes).doesNotContainKey("roomId");
            // 로그는 System.out.println으로 콘솔에서 확인 가능
        }

        @Test
        @DisplayName("SUBSCRIBE가 아닌 경우 roomId가 세션에 추가되지 않아야 함")
        void notSubscribe_shouldNotPutRoomId() {
            Map<String, Object> sessionAttributes = new HashMap<>();
            StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SEND);
            accessor.setDestination("/sub/chat/room/123");
            accessor.setSessionAttributes(sessionAttributes);
            Message<byte[]> message = new GenericMessage<>(new byte[0], accessor.toMessageHeaders());

            createInterceptorWithLogging().preSend(message, null);

            assertThat(sessionAttributes).doesNotContainKey("roomId");
        }

        @Test
        @DisplayName("DEST가 null이면 roomId 추가 안 됨")
        void destNull_shouldNotPutRoomId() {
            Map<String, Object> sessionAttributes = new HashMap<>();
            StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
            accessor.setDestination(null);
            accessor.setSessionAttributes(sessionAttributes);
            Message<byte[]> message = new GenericMessage<>(new byte[0], accessor.toMessageHeaders());

            createInterceptorWithLogging().preSend(message, null);

            assertThat(sessionAttributes).doesNotContainKey("roomId");
        }

        @Test
        @DisplayName("DEST가 잘못된 prefix이면 roomId 추가 안 됨")
        void wrongPrefix_shouldNotPutRoomId() {
            Map<String, Object> sessionAttributes = new HashMap<>();
            StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
            accessor.setDestination("/sub/chat/other/123");
            accessor.setSessionAttributes(sessionAttributes);
            Message<byte[]> message = new GenericMessage<>(new byte[0], accessor.toMessageHeaders());

            createInterceptorWithLogging().preSend(message, null);

            assertThat(sessionAttributes).doesNotContainKey("roomId");
        }
    }

    @Nested
    @DisplayName("NumberFormatException 예외 테스트")
    class PreSendExceptionTests {

        @Test
        @DisplayName("DEST가 숫자가 아니면 NumberFormatException 발생")
        void destNotNumber_shouldThrowNumberFormatException() {
            StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
            accessor.setDestination("/sub/chat/room/abc"); // 숫자가 아님
            accessor.setSessionAttributes(new HashMap<>());
            Message<byte[]> message = new GenericMessage<>(new byte[0], accessor.toMessageHeaders());

            ChannelInterceptor interceptor = createInterceptorThrowingException();

            // 숫자가 아니므로 예외 발생 확인
            org.assertj.core.api.Assertions.assertThatThrownBy(() -> interceptor.preSend(message, null))
                    .isInstanceOf(NumberFormatException.class);
        }
    }
}
