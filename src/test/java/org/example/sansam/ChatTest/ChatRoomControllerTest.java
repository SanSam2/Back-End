package org.example.sansam.ChatTest;
import org.example.sansam.chat.controller.ChatRoomController;
import org.example.sansam.chat.dto.ChatRoomRequestDTO;
import org.example.sansam.chat.dto.ChatRoomResponseDTO;
import org.example.sansam.chat.dto.UserRoomResponseDTO;
import org.example.sansam.chat.service.ChatRoomService;
import org.example.sansam.user.dto.LoginResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class ChatRoomControllerTest {

    @InjectMocks
    private ChatRoomController controller;

    @Mock
    private ChatRoomService chatRoomService;

    @Mock
    private HttpSession session;

    @Captor
    private ArgumentCaptor<Pageable> pageableCaptor;

    private LoginResponse loginUser;

    @BeforeEach
    void setup() {
        loginUser = new LoginResponse();
        loginUser.setId(1L);
    }

    @Test
    @DisplayName("전체 조회 - 채팅방 목록 조회: 키워드가 없으면 전체 조회, 서비스에 keyword=null 로 전달")
    void chatroom_shouldReturnAllRooms_whenKeywordIsNull() {
        // given
        ChatRoomResponseDTO r1 = ChatRoomResponseDTO.builder()
                .id(10L)
                .roomName("Java 스터디")
                .createdAt(LocalDateTime.now().minusDays(2))
                .build();
        ChatRoomResponseDTO r2 = ChatRoomResponseDTO.builder()
                .id(11L)
                .roomName("Spring 스터디")
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();
        Page<ChatRoomResponseDTO> mockPage = new PageImpl<>(List.of(r1, r2));

        given(chatRoomService.roomList(isNull(), any(Pageable.class)))
                .willReturn(mockPage);

        // when → ResponseEntity<?> 로 받는다
        ResponseEntity<?> resp = controller.chatroom(null, 0, 20);

        // then
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);

        // body 를 Page<ChatRoomResponseDTO> 로 캐스팅
        @SuppressWarnings("unchecked")
        Page<ChatRoomResponseDTO> body = (Page<ChatRoomResponseDTO>) resp.getBody();

        assertThat(body).isNotNull();
        assertThat(body.getContent()).hasSize(2)
                .extracting("roomName")
                .containsExactly("Java 스터디", "Spring 스터디");

        then(chatRoomService).should().roomList(isNull(), pageableCaptor.capture());
        Pageable passed = pageableCaptor.getValue();
        assertThat(passed.getPageNumber()).isZero();
        assertThat(passed.getPageSize()).isEqualTo(20);
        assertThat(passed.getSort().getOrderFor("createdAt").isDescending()).isTrue();
    }

    @Test
    @DisplayName("전체 조회 - 채팅방 목록 조회: 키워드가 있으면 해당 키워드를 서비스에 전달")
    void chatroom_shouldFilterRooms_whenKeywordProvided() {
        // given
        ChatRoomResponseDTO match = ChatRoomResponseDTO.builder()
                .id(20L)
                .roomName("Java 테스트룸")
                .createdAt(LocalDateTime.now())
                .build();
        Page<ChatRoomResponseDTO> mockPage = new PageImpl<>(List.of(match));

        given(chatRoomService.roomList(eq("Java"), any(Pageable.class)))
                .willReturn(mockPage);

        // when
        ResponseEntity<?> resp = controller.chatroom("Java", 1, 5);

        // then
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);

        @SuppressWarnings("unchecked")
        Page<ChatRoomResponseDTO> body = (Page<ChatRoomResponseDTO>) resp.getBody();

        assertThat(body).isNotNull();
        assertThat(body.getContent()).hasSize(1)
                .first()
                .extracting("roomName")
                .isEqualTo("Java 테스트룸");

        then(chatRoomService).should().roomList(eq("Java"), pageableCaptor.capture());
        Pageable passed = pageableCaptor.getValue();
        assertThat(passed.getPageNumber()).isEqualTo(1);
        assertThat(passed.getPageSize()).isEqualTo(5);
        assertThat(passed.getSort().getOrderFor("createdAt").isDescending()).isTrue();
    }


    @Test
    @DisplayName("유저 방 조회 실패 - 세션 없으면 UserChatRoomGet 401 반환")
    void userChatRoomGet_shouldReturn401_whenNoSession() {
        when(session.getAttribute("loginUser")).thenReturn(null);

        ResponseEntity<?> response = controller.UserChatRoomGet(session, 0, 20);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @DisplayName("유저 방 조회 - 로그인 있으면 UserChatRoomGet 성공")
    void userChatRoomGet_shouldReturnRoomList_whenSessionExists() {
        when(session.getAttribute("loginUser")).thenReturn(loginUser);

        UserRoomResponseDTO dto = UserRoomResponseDTO.builder()
                .id(1L)
                .roomName("테스트방")
                .createdAt(LocalDateTime.now())
                .lastMessageAt(LocalDateTime.now())
                .messageCount(5L)
                .build();
        Page<UserRoomResponseDTO> page = new PageImpl<>(List.of(dto));
        when(chatRoomService.userRoomList(1L, 0, 20)).thenReturn(page);

        ResponseEntity<?> response = controller.UserChatRoomGet(session, 0, 20);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Page<UserRoomResponseDTO> body =
                (Page<UserRoomResponseDTO>) response.getBody();
        assertThat(body.getContent()).hasSize(1);
        assertThat(body.getContent().get(0).getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("유저방 조회 실패 - UserChatRoomGet: service에서 예외 발생 시 BAD_REQUEST 반환")
    void userChatRoomGet_serviceThrowsException() {
        // 로그인 유저 Mock
        LoginResponse loginUser = new LoginResponse();
        loginUser.setId(1L);
        when(session.getAttribute("loginUser")).thenReturn(loginUser);

        // service에서 예외 발생하도록 Mock
        when(chatRoomService.userRoomList(loginUser.getId(), 0, 20))
                .thenThrow(new RuntimeException("서비스 예외 발생"));

        ResponseEntity<?> response = controller.UserChatRoomGet(session, 0, 20);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("서비스 예외 발생", response.getBody());
    }

    @Test
    @DisplayName("생성 - 채팅방 생성 성공")
    void createRoom_shouldReturnChatRoom_whenLoginExists() {
        when(session.getAttribute("loginUser")).thenReturn(loginUser);

        ChatRoomRequestDTO requestDTO = ChatRoomRequestDTO.builder()
                .roomName("새방")
                .build();
        ChatRoomResponseDTO responseDTO = ChatRoomResponseDTO.builder()
                .id(100L)
                .roomName("새방")
                .createdAt(LocalDateTime.now())
                .build();
        when(chatRoomService.createRoom(requestDTO, 1L)).thenReturn(responseDTO);

        ResponseEntity<?> response = controller.createRoom(requestDTO, session);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        ChatRoomResponseDTO body = (ChatRoomResponseDTO) response.getBody();
        assertThat(body.getRoomName()).isEqualTo("새방");
    }

    @Test
    @DisplayName("생성 실패(로그인) - 로그인되지 않은 유저가 채팅방 생성 시 UNAUTHORIZED 반환")
    void createRoom_userNotLoggedIn() {
        when(session.getAttribute("loginUser")).thenReturn(null);

        ChatRoomRequestDTO dto = ChatRoomRequestDTO.builder()
                .roomName("테스트방")
                .setAmount(0L)
                .build();

        ResponseEntity<?> response = controller.createRoom(dto, session);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @DisplayName("생성 실패 - 채팅방 생성 중 예외 발생 시 400 반환")
    void createRoom_serviceThrowsException() {
        LoginResponse loginUser = new LoginResponse();
        loginUser.setId(1L);

        when(session.getAttribute("loginUser")).thenReturn(loginUser);

        ChatRoomRequestDTO dto = ChatRoomRequestDTO.builder()
                .roomName("테스트방")
                .setAmount(0L)
                .build();

        when(chatRoomService.createRoom(dto, loginUser.getId()))
                .thenThrow(new IllegalArgumentException("테스트 예외"));

        ResponseEntity<?> response = controller.createRoom(dto, session);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("테스트 예외", response.getBody());
    }

    @Test
    @DisplayName("입장 - 채팅방 입장 성공")
    void chatroomEnter_shouldReturnRoom_whenLoginExists() {
        when(session.getAttribute("loginUser")).thenReturn(loginUser);

        ChatRoomResponseDTO responseDTO = ChatRoomResponseDTO.builder()
                .id(15L)
                .roomName("팀 프로젝트")
                .createdAt(LocalDateTime.now())
                .build();
        when(chatRoomService.enterRoom(15L, 1L)).thenReturn(responseDTO);

        ResponseEntity<?> response = controller.chatroomEnter(15L, session);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        ChatRoomResponseDTO body = (ChatRoomResponseDTO) response.getBody();
        assertThat(body.getId()).isEqualTo(15L);
        assertThat(body.getRoomName()).isEqualTo("팀 프로젝트");
    }

    @Test
    @DisplayName("입장 - 로그인 없는 경우 401 반환")
    void enterRoom_noLogin() {
        when(session.getAttribute("loginUser")).thenReturn(null);

        ResponseEntity<?> response = controller.chatroomEnter(1L, session);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("입장 - 서비스 ResponseStatusException 발생 시 그대로 throw")
    void enterRoom_serviceThrowsResponseStatusException() {
        LoginResponse login = new LoginResponse();
        login.setId(1L);
        when(session.getAttribute("loginUser")).thenReturn(login);

        when(chatRoomService.enterRoom(anyLong(), anyLong()))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "접근 불가"));

        assertThatThrownBy(() -> controller.chatroomEnter(1L, session))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("접근 불가");
    }

    @Test
    @DisplayName("입장 - 기타 예외 발생 시 500 반환")
    void enterRoom_serviceThrowsException() {
        LoginResponse login = new LoginResponse();
        login.setId(1L);
        when(session.getAttribute("loginUser")).thenReturn(login);

        when(chatRoomService.enterRoom(anyLong(), anyLong()))
                .thenThrow(new RuntimeException("DB 에러"));

        ResponseEntity<?> response = controller.chatroomEnter(1L, session);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isEqualTo("채팅방 입장 중 오류가 발생했습니다.");
    }

    @Test
    @DisplayName("정상 퇴장 처리 시 204 No Content 반환")
    void leave_shouldReturn204_whenSuccess() {
        // given
        when(session.getAttribute("loginUser")).thenReturn(loginUser);

        // when
        ResponseEntity<?> resp = controller.chatroomLeave(55L, session);

        // then
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(chatRoomService, times(1))
                .roomLeave(eq(55L), eq(loginUser.getId()));
    }

    @Test
    @DisplayName("퇴장 - 서비스에서 예외 발생 시 500 Internal Server Error 반환")
    void leave_shouldReturn500_whenServiceThrows() {
        // given: 세션에 LoginResponse 객체를 리턴하도록 모킹
        when(session.getAttribute("loginUser"))
                .thenReturn(loginUser);

        // roomLeave 호출 시 예외를 던지도록 스텁
        doThrow(new RuntimeException("oops"))
                .when(chatRoomService)
                .roomLeave(eq(77L), eq(loginUser.getId()));

        // when
        ResponseEntity<?> resp = controller.chatroomLeave(77L, session);

        // then
        assertThat(resp.getStatusCode())
                .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

        // 이제 실제로 서비스가 호출됐는지 검증 가능
        verify(chatRoomService, times(1))
                .roomLeave(eq(77L), eq(loginUser.getId()));
    }

    @Test
    @DisplayName("chatroomLeave: 로그인 안된 경우 UNAUTHORIZED 반환")
    void chatroomLeave_userNotLoggedIn() {
        Long roomId = 1L;

        // 세션에서 loginUser가 null 반환하도록 Mock
        when(session.getAttribute("loginUser")).thenReturn(null);

        ResponseEntity<?> response = controller.chatroomLeave(roomId, session);

        // 검증
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
}