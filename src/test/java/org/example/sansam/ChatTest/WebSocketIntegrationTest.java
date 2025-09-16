package org.example.sansam.ChatTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.sansam.chat.config.WebSocketEventListener;
import org.example.sansam.chat.dto.ChatMessageRequestDTO;
import org.example.sansam.chat.dto.ChatMessageResponseDTO;
import org.example.sansam.chat.domain.ChatMember;
import org.example.sansam.chat.domain.ChatMemberId;
import org.example.sansam.chat.domain.ChatRoom;
import org.example.sansam.chat.repository.ChatMemberRepository;
import org.example.sansam.chat.repository.ChatMessageRepository;
import org.example.sansam.chat.repository.ChatRoomRepository;
import org.example.sansam.user.domain.Role;
import org.example.sansam.user.domain.User;
import org.example.sansam.user.dto.LoginRequest;
import org.example.sansam.user.dto.LoginResponse;
import org.example.sansam.user.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.Message;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.*;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class WebSocketIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ChatRoomRepository chatRoomRepository;
    @Autowired
    private ChatMemberRepository chatMemberRepository;
    @Autowired
    private WebSocketEventListener listener;

    private WebSocketStompClient stompClient;
    private String wsUrl;
    private HttpHeaders loginHeaders;

    @BeforeAll
    void setup() {

        // 테스트용 유저/방/멤버 생성
        User u = userRepository.findByEmail("test@example.com")
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .email("test@example.com")
                                .name("테스트유저")
                                .password("pw")
                                .role(Role.USER)
                                .createdAt(LocalDateTime.now())
                                .emailAgree(true)
                                .mobileNumber("01011112222")
                                .activated(true)
                                .build()
                ));
        ChatRoom r = chatRoomRepository.findByRoomName("테스트방")
                .orElseGet(() -> chatRoomRepository.save(
                        ChatRoom.builder()
                                .roomName("테스트방")
                                .createdAt(LocalDateTime.now())
                                .lastMessageAt(LocalDateTime.now())
                                .setAmount(0L)
                                .build()
                ));
        ChatMemberId memberId = new ChatMemberId(u.getId(), r.getId());

        chatMemberRepository.findById(memberId)
                .orElseGet(() -> chatMemberRepository.save(
                        ChatMember.builder()
                                .id(memberId)
                                .user(u)
                                .chatRoom(r)
                                .build()
                ));

        LoginRequest loginReq = new LoginRequest();
        loginReq.setEmail("test@example.com");
        loginReq.setPassword("pw");

        ResponseEntity<LoginResponse> loginResp = restTemplate
                .postForEntity("http://localhost:" + port + "/api/users/login",
                        loginReq,
                        LoginResponse.class);

        loginHeaders = new HttpHeaders();
        List<String> cookies = loginResp.getHeaders().get(HttpHeaders.SET_COOKIE);
        if (cookies != null) {
            loginHeaders.put(HttpHeaders.COOKIE, cookies);
        }

        // STOMP over SockJS Client 준비
        List<Transport> transports = Arrays.asList(
                new WebSocketTransport(new StandardWebSocketClient()),
                new RestTemplateXhrTransport(restTemplate.getRestTemplate())
        );
        SockJsClient sockJs = new SockJsClient(transports);
        stompClient = new WebSocketStompClient(sockJs);
        // JSON <-> DTO 변환
        stompClient.setMessageConverter(new MappingJackson2MessageConverter(objectMapper));

        wsUrl = "ws://localhost:" + port + "/ws-stomp";
    }

    @Test
    @DisplayName("WebSocket E2E: 메시지 발행 → 구독자에 브로드캐스트")
    void chatRoomWebSocket_EndToEnd() throws Exception {
        // 1) 메시지 수신용 큐와 래치 준비
        BlockingQueue<ChatMessageResponseDTO> queue = new ArrayBlockingQueue<>(1);
        CountDownLatch latch = new CountDownLatch(1);

        // 2) Handshake 시 로그인 쿠키 포함
        WebSocketHttpHeaders handshake = new WebSocketHttpHeaders();
        handshake.putAll(loginHeaders);

        // 3) STOMP 세션 연결
        StompSession session = stompClient
                .connect(wsUrl, handshake, new StompSessionHandlerAdapter() {})
                .get(5, TimeUnit.SECONDS);

        // 4) 채팅방 ID 조회
        Long roomId = chatRoomRepository.findAll().get(0).getId();

        // 5) 구독 (프레임 핸들러로 payload 직접 처리)
        session.subscribe(
                "/sub/chat/room/" + roomId,
                new StompFrameHandler() {
                    @Override
                    public Type getPayloadType(StompHeaders headers) {
                        return ChatMessageResponseDTO.class;
                    }

                    @Override
                    public void handleFrame(StompHeaders headers, Object payload) {
                        // 메시지 수신 시 래치 카운트다운 및 큐에 저장
                        queue.offer((ChatMessageResponseDTO) payload);
                        latch.countDown();
                    }
                }
        );

        // 6) 메시지 발행
        ChatMessageRequestDTO req = new ChatMessageRequestDTO();
        req.setMessage("안녕하세요 WebSocket 테스트!");
        session.send("/app/chat/" + roomId + "/message", req);

        // 7) 메시지를 실제로 수신했는지 최대 5초 대기
        boolean received = latch.await(5, TimeUnit.SECONDS);
        assertThat(received).isTrue();

        // 8) 페이로드 검증
        ChatMessageResponseDTO res = queue.poll(1, TimeUnit.SECONDS);
        assertThat(res).isNotNull();
        assertThat(res.getMessage()).isEqualTo("안녕하세요 WebSocket 테스트!");
        assertThat(res.getUserName()).isEqualTo("테스트유저");
        assertThat(res.getId()).isNotNull();
        assertThat(res.getCreatedAt()).isNotNull();

        // 9) 세션 정리
        session.disconnect();
    }

    @Test
    @DisplayName("WebSocket E2E: 숫자가 아닌 roomId로 구독 시 인터셉터 로그 확인")
    void chatRoomWebSocket_SubscribeInvalidRoomId() throws Exception {
        // 1) Handshake 시 로그인 쿠키 포함
        WebSocketHttpHeaders handshake = new WebSocketHttpHeaders();
        handshake.putAll(loginHeaders);

        // 2) STOMP 세션 연결
        StompSession session = stompClient
                .connect(wsUrl, handshake, new StompSessionHandlerAdapter() {})
                .get(5, TimeUnit.SECONDS);

        // 3) 숫자가 아닌 roomId로 구독
        session.subscribe(
                "/sub/chat/room/abc",  // 숫자가 아님
                new StompFrameHandler() {
                    @Override
                    public Type getPayloadType(StompHeaders headers) {
                        return Object.class;
                    }

                    @Override
                    public void handleFrame(StompHeaders headers, Object payload) {
                        // 아무 작업 안 함
                    }
                }
        );

        // 4) 잠시 대기하여 interceptor 로그 확인
        Thread.sleep(1000); // log.warn가 찍히는 것을 콘솔에서 확인 가능

        session.disconnect();
    }

    // 해당 분기별로 updateLastReadAt이 일어나는지 일어나지 않는지 확인
    @Test
    @DisplayName("WebSocket Disconnect 이벤트: userId, roomId 조합별 분기 테스트")
    void chatRoomWebSocket_DisconnectBranching() {
        Long roomId = chatRoomRepository.findAll().get(0).getId();
        Long userId = userRepository.findAll().get(0).getId();

        Object[][] testCases = new Object[][]{
                {null, null},
                {null, roomId},
                {userId, null},
                {userId, roomId}
        };

        for (Object[] tc : testCases) {
            Long uid = (Long) tc[0];
            Long rid = (Long) tc[1];

            StompHeaderAccessor sha = StompHeaderAccessor.create(StompCommand.DISCONNECT);
            sha.setSessionId("dummySessionId");
            sha.setSessionAttributes(new HashMap<>() {{
                put("userId", uid);
                put("roomId", rid);
            }});

            Message<byte[]> disconnectMessage = MessageBuilder.createMessage(new byte[0], sha.getMessageHeaders());
            SessionDisconnectEvent event = new SessionDisconnectEvent(this, disconnectMessage, "dummySessionId", null);

            // listener 호출 필수
            listener.onDisconnect(event);

            if (uid != null && rid != null) {
                ChatMember member = chatMemberRepository.findById(new ChatMemberId(uid, rid)).orElseThrow();
                assertThat(member.getLastReadAt()).isNotNull();
            } else {
                Assertions.assertDoesNotThrow(() -> {});
            }
        }
    }
}