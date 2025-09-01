package org.example.sansam.ChatTest;


import jakarta.servlet.http.HttpSession;
import org.example.sansam.chat.controller.ChatMessageController;
import org.example.sansam.chat.dto.ChatMessageRequestDTO;
import org.example.sansam.chat.dto.ChatMessageResponseDTO;
import org.example.sansam.chat.dto.ChatMessageSendResponseDTO;
import org.example.sansam.chat.service.ChatMessageService;
import org.example.sansam.user.dto.LoginResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatMessageControllerTest {

    @Mock
    private ChatMessageService chatMessageService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private HttpSession session;

    @InjectMocks
    private ChatMessageController chatMessageController;

    private SimpMessageHeaderAccessor headerWithUser;
    private SimpMessageHeaderAccessor headerWithoutUser;
    ChatMessageSendResponseDTO mockResponse;

    @BeforeEach
    void setUp() {
        mockResponse = ChatMessageSendResponseDTO.builder()
                .id(1L)
                .message("hello")
                .userName("tester")
                .createdAt(LocalDateTime.now())
                .sender(1L)
                .roomId(50L)
                .build();

        headerWithUser = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
        headerWithUser.setSessionId("sess1");
        headerWithUser.setSessionAttributes(new java.util.HashMap<>());
        LoginResponse loginUser = new LoginResponse(1L, "tester@example.com", "tester", "password", "01000000000", "USER");
        headerWithUser.getSessionAttributes().put("loginUser", loginUser);
        headerWithoutUser = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
        headerWithoutUser.setSessionId("sess1");
        headerWithoutUser.setSessionAttributes(new java.util.HashMap<>());
    }

    @Test
    @DisplayName("로그인한 사용자가 채팅방 메시지 삭제 요청 시 서비스 호출")
    void deleteRoomMessage_whenAuthenticated_thenCallsService() {
        // headerAccessor Mock
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create();
        headerAccessor.setSessionAttributes(new HashMap<>()); // 여기서 초기화
        LoginResponse loginUser = LoginResponse.builder()
                .id(1L)
                .name("testUser")
                .build();
        headerAccessor.getSessionAttributes().put("loginUser", loginUser);

        // 실행
        chatMessageController.deleteRoomMessage(100L, 200L, headerAccessor);

        // 검증
        then(chatMessageService).should().deleteMessage(200L, 1L, 100L);
    }

    @Test
    @DisplayName("로그인하지 않은 사용자가 채팅방 메시지 삭제 요청 시 서비스 호출 안됨")
    void deleteRoomMessage_whenNotAuthenticated_thenNoServiceCall() {
        SimpMessageHeaderAccessor headerWithoutUser = SimpMessageHeaderAccessor.create();
        headerWithoutUser.setSessionAttributes(new HashMap<>());

        chatMessageController.deleteRoomMessage(100L, 200L, headerWithoutUser);

        then(chatMessageService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("로그인한 사용자가 메시지 전송 시 서비스 호출 및 브로드캐스트 수행")
    void handleMessage_whenAuthenticated_thenBroadcast() {
        ChatMessageRequestDTO req = new ChatMessageRequestDTO();
        req.setMessage("hi");

        // any()로 매칭
        given(chatMessageService.addMessage(any(ChatMessageRequestDTO.class), anyLong(), anyLong()))
                .willReturn(mockResponse);

        chatMessageController.handleMessage(50L, req, headerWithUser);

        then(chatMessageService).should().addMessage(req, 1L, 50L);
        then(messagingTemplate).should()
                .convertAndSend("/sub/chat/room/50", mockResponse);
    }


    @Test
    @DisplayName("로그인하지 않은 사용자가 메시지 전송 시 서비스 및 브로드캐스트 호출 안됨")
    void handleMessage_whenNotAuthenticated_thenNoInteraction() {
        ChatMessageRequestDTO req = new ChatMessageRequestDTO();
        // headerWithoutUser 사용

        chatMessageController.handleMessage(50L, req, headerWithoutUser);

        then(chatMessageService).shouldHaveNoInteractions();
        then(messagingTemplate).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("채팅방 메시지 조회 - 성공")
    void getRoomMessages_success() {
        // 세션에 로그인 유저 심기
        LoginResponse loginUser = new LoginResponse();
        loginUser.setId(1L);
        loginUser.setName("테스트유저");
        when(session.getAttribute("loginUser")).thenReturn(loginUser);

        // Service에서 반환할 메시지
        ChatMessageResponseDTO dto = ChatMessageResponseDTO.builder()
                .id(100L)
                .message("단위 테스트 메시지")
                .userName("테스트유저")
                .createdAt(LocalDateTime.now())
                .sender(1L)
                .roomId(1L)
                .build();

        Page<ChatMessageResponseDTO> page = new PageImpl<>(List.of(dto));
        when(chatMessageService.getMessages(1L, null, 1L, 20)).thenReturn(page);

        // Controller 호출
        ResponseEntity<?> response = chatMessageController.getRoomMessages(1L, session, null, 20);

        // 검증
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(page);
        verify(chatMessageService).getMessages(1L, null, 1L, 20);
    }

    @Test
    @DisplayName("채팅방 메시지 조회 - 로그인이 안된 상태")
    void getRoomMessages_unauthorized() {
        // 1) 세션에서 loginUser가 없도록 설정
        when(session.getAttribute("loginUser")).thenReturn(null);

        // 2) Controller 호출
        ResponseEntity<?> response = chatMessageController.getRoomMessages(1L, session, null, 20);

        // 3) 검증
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        // Service는 호출되지 않아야 함
        verifyNoInteractions(chatMessageService);
    }

    @Test
    @DisplayName("채팅방_메시지조회_서비스예외발생하면_400반환()")
    void getRoomMessages_exceptionHandling() {
        // 1) 세션에 로그인 유저 심기
        LoginResponse loginUser = LoginResponse.builder()
                .id(1L)
                .name("테스트유저")
                .build();
        when(session.getAttribute("loginUser")).thenReturn(loginUser);

        // 2) Service 호출 시 예외 발생
        when(chatMessageService.getMessages(1L, null, 1L, 20))
                .thenThrow(new RuntimeException("DB 오류"));

        // 3) Controller 호출
        ResponseEntity<?> response = chatMessageController.getRoomMessages(1L, session, null, 20);

        // 4) 검증
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("DB 오류");

        // Service 호출 확인
        verify(chatMessageService).getMessages(1L, null, 1L, 20);
    }
}
