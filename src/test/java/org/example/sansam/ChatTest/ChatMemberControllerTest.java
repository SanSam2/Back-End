package org.example.sansam.ChatTest;

import org.example.sansam.chat.controller.ChatMemberController;
import org.example.sansam.chat.dto.LastReadDTO;
import org.example.sansam.chat.service.ChatMemberService;
import org.example.sansam.user.dto.LoginResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;

import java.time.LocalDateTime;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ChatMemberControllerTest {

    @Mock
    private ChatMemberService chatMemberService;

    @InjectMocks
    private ChatMemberController chatMemberController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("lastRead - 세션에 로그인 유저가 있으면 서비스 호출 후 200 OK 반환")
    void lastRead_authenticatedUser_callsServiceAndReturnsOk() {
        // given
        MockHttpSession session = new MockHttpSession();
        LoginResponse loginUser = new LoginResponse();
        loginUser.setId(1L);
        session.setAttribute("loginUser", loginUser);

        LastReadDTO dto = new LastReadDTO();
        dto.setRoomId(100L);

        // when
        ResponseEntity<Void> response = chatMemberController.lastRead(dto, session);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        ArgumentCaptor<Long> userIdCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Long> roomIdCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<LocalDateTime> dateTimeCaptor = ArgumentCaptor.forClass(LocalDateTime.class);

        verify(chatMemberService, times(1))
                .updateLastReadAt(userIdCaptor.capture(), roomIdCaptor.capture(), dateTimeCaptor.capture());

        assertThat(userIdCaptor.getValue()).isEqualTo(1L);
        assertThat(roomIdCaptor.getValue()).isEqualTo(100L);
        assertThat(dateTimeCaptor.getValue()).isNotNull();
    }

    @Test
    @DisplayName("lastRead - 로그인 유저가 없으면 401 Unauthorized 반환")
    void lastRead_unauthenticatedUser_returnsUnauthorized() {
        // given
        MockHttpSession session = new MockHttpSession();
        LastReadDTO dto = new LastReadDTO();
        dto.setRoomId(100L);

        // when
        ResponseEntity<Void> response = chatMemberController.lastRead(dto, session);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(chatMemberService, never()).updateLastReadAt(anyLong(), anyLong(), any());
    }

    @Test
    @DisplayName("handleLastRead - 세션에 로그인 유저가 있으면 서비스 호출됨")
    void handleLastRead_authenticatedUser_callsService() {
        // given
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create();
        LoginResponse loginUser = new LoginResponse();
        loginUser.setId(1L);
        headerAccessor.setSessionAttributes(new HashMap<>());
        headerAccessor.getSessionAttributes().put("loginUser", loginUser);

        LastReadDTO dto = new LastReadDTO();
        dto.setRoomId(100L);

        // when
        chatMemberController.handleLastRead(dto, headerAccessor);

        // then
        verify(chatMemberService, times(1))
                .updateLastReadAt(eq(1L), eq(100L), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("handleLastRead - 로그인 유저가 없으면 IllegalStateException 발생")
    void handleLastRead_unauthenticatedUser_throwsException() {
        // given
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create();
        headerAccessor.setSessionAttributes(new HashMap<>()); // <-- 추가
        LastReadDTO dto = new LastReadDTO();
        dto.setRoomId(100L);

        // when & then
        try {
            chatMemberController.handleLastRead(dto, headerAccessor);
        } catch (IllegalStateException e) {
            assertThat(e.getMessage()).isEqualTo("User not authenticated");
        }

        verify(chatMemberService, never()).updateLastReadAt(anyLong(), anyLong(), any());
    }
}