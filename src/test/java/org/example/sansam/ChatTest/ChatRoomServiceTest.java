package org.example.sansam.ChatTest;

import org.example.sansam.chat.domain.ChatMember;
import org.example.sansam.chat.domain.ChatMemberId;
import org.example.sansam.chat.domain.ChatRoom;
import org.example.sansam.chat.dto.ChatRoomRequestDTO;
import org.example.sansam.chat.dto.ChatRoomResponseDTO;
import org.example.sansam.chat.dto.RoomCountDTO;
import org.example.sansam.chat.dto.UserRoomResponseDTO;
import org.example.sansam.chat.repository.ChatMemberRepository;
import org.example.sansam.chat.repository.ChatMessageRepository;
import org.example.sansam.chat.repository.ChatRoomRepository;
import org.example.sansam.chat.service.ChatRoomService;
import org.example.sansam.user.domain.User;
import org.example.sansam.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatRoomServiceTest {
    @InjectMocks
    private ChatRoomService service;

    @Mock private ChatRoomRepository roomRepo;
    @Mock private UserRepository userRepo;
    @Mock private ChatMemberRepository memberRepo;
    @Mock private ChatMessageRepository chatMessageRepository;

    @Test
    @DisplayName("채팅방 입장 - 연봉미달_예외던짐")
    void 연봉미달_예외던짐() {
        ChatRoom room = ChatRoom.builder().id(1L).setAmount(5000L).build();
        User user = new User(); user.setId(1L); user.setSalary(3000L);
        when(roomRepo.findById(1L)).thenReturn(Optional.of(room));
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.enterRoom(1L, 1L)
        );
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    @DisplayName("채팅방 입장 - 연봉 이상이면 성공")
    void enterRoom_Success_WhenSalaryIsEnough() {
        ChatRoom room = ChatRoom.builder()
                .id(1L)
                .setAmount(5000L)
                .roomName("Test")
                .createdAt(LocalDateTime.now())
                .lastMessageAt(LocalDateTime.now())
                .build();
        when(roomRepo.findById(1L)).thenReturn(Optional.of(room));

        User user = new User();
        user.setId(1L);
        user.setSalary(6000L);
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));

        when(memberRepo.existsById(any(ChatMemberId.class))).thenReturn(false);

        ChatRoomResponseDTO result = service.enterRoom(1L, 1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());

        verify(memberRepo, times(1)).save(any(ChatMember.class));
    }

    @Test
    @DisplayName("enterRoom: 로그인 유저가 존재하지 않으면 NoSuchElementException 발생")
    void enterRoom_userNotFound_throwsException() {
        Long roomId = 1L;
        Long userId = 1L;

        ChatRoom room = ChatRoom.builder()
                .id(roomId)
                .setAmount(0L)
                .build();

        // roomRepo에서 채팅방은 찾았다고 가정
        when(roomRepo.findById(roomId)).thenReturn(Optional.of(room));

        // userRepo에서 유저를 못 찾음
        when(userRepo.findById(userId)).thenReturn(Optional.empty());

        // 실행 & 검증
        NoSuchElementException exception = assertThrows(NoSuchElementException.class,
                () -> service.enterRoom(roomId, userId));

        assertEquals("유저가 존재하지 않습니다.", exception.getMessage());

        // memberRepo.save는 호출되지 않아야 함
        verify(memberRepo, never()).save(any());
    }

    @Test
    @DisplayName("enterRoom: 이미 멤버이면 save 호출 없이 DTO 반환")
    void enterRoom_alreadyMember_noSave() {
        Long roomId = 1L;
        Long userId = 1L;

        ChatRoom room = ChatRoom.builder()
                .id(roomId)
                .setAmount(0L)
                .build();

        User user = User.builder()
                .id(userId)
                .salary(5000L)
                .build();

        when(roomRepo.findById(roomId)).thenReturn(Optional.of(room));
        when(userRepo.findById(userId)).thenReturn(Optional.of(user));

        // 이미 멤버 존재
        when(memberRepo.existsById(new ChatMemberId(userId, roomId))).thenReturn(true);

        ChatRoomResponseDTO result = service.enterRoom(roomId, userId);

        assertNotNull(result);
        assertEquals(room.getId(), result.getId());

        // save 호출 안됨 검증
        verify(memberRepo, never()).save(any());
    }

    @Test
    @DisplayName("로그인 유저 채팅방 조회 시, 읽지 않은 메시지 수가 정확히 계산되어 반환된다")
    void unreadCountIsMappedCorrectly() {
        ChatRoom room = ChatRoom.builder().id(10L).roomName("R").build();
        Page<ChatRoom> rooms = new PageImpl<>(
                List.of(room),
                PageRequest.of(0, 10), 1
        );
        when(memberRepo.findChatRoomsByUserId(eq(1L), any(Pageable.class)))
                .thenReturn(rooms);

        RoomCountDTO rc = new RoomCountDTO(10L, 5L);
        when(chatMessageRepository.countUnreadByUserAndRoomIds(1L, List.of(10L)))
                .thenReturn(List.of(rc));

        Page<UserRoomResponseDTO> result = service.userRoomList(1L, 0, 10);

        assertThat(result.getTotalElements()).isEqualTo(1);
        UserRoomResponseDTO dto = result.getContent().get(0);
        assertThat(dto.getId()).isEqualTo(10L);
        assertThat(dto.getMessageCount()).isEqualTo(5L);
    }

    @Test
    @DisplayName("채팅방 생성 - 로그인한 유저")
    void createRoom_validInput_savesRoom() {
        // given
        ChatRoomRequestDTO dto = ChatRoomRequestDTO.builder()
                .roomName("테스트방")
                .setAmount(5L)
                .build();
        Long userId = 1L;

        User user = User.builder().id(userId).build(); // 빌더 사용 가능
        when(userRepo.findById(userId)).thenReturn(Optional.of(user));

        when(roomRepo.save(any(ChatRoom.class))).thenAnswer(invocation -> {
            ChatRoom room = invocation.getArgument(0);
            room.setId(1L); // ID 세팅
            return room;
        });
        when(memberRepo.save(any(ChatMember.class))).thenAnswer(i -> i.getArgument(0));

        // when
        ChatRoomResponseDTO result = service.createRoom(dto, userId);

        // then
        assertNotNull(result);
        assertEquals("테스트방", result.getRoomName());
        verify(roomRepo).save(any(ChatRoom.class));
        verify(memberRepo).save(any(ChatMember.class));
    }

    @Test
    @DisplayName("채팅방 생성 시 setAmount가 null이면 0으로 세팅")
    void createRoom_setAmountNull_setsZero() {
        // given
        ChatRoomRequestDTO dto = ChatRoomRequestDTO.builder()
                .roomName("테스트방")
                .setAmount(null) // null 값 설정
                .build();
        Long userId = 1L;

        User user = User.builder().id(userId).build();
        when(userRepo.findById(userId)).thenReturn(Optional.of(user));

        when(roomRepo.save(any(ChatRoom.class))).thenAnswer(invocation -> {
            ChatRoom room = invocation.getArgument(0);
            room.setId(1L); // ID 세팅
            return room;
        });
        when(memberRepo.save(any(ChatMember.class))).thenAnswer(i -> i.getArgument(0));

        // when
        ChatRoomResponseDTO result = service.createRoom(dto, userId);

        // then
        assertNotNull(result);
        assertEquals("테스트방", result.getRoomName());

        // ChatRoom 객체에 대한 ArgumentCaptor를 사용하여 setAmount 확인
        ArgumentCaptor<ChatRoom> captor = ArgumentCaptor.forClass(ChatRoom.class);
        verify(roomRepo).save(captor.capture());
        ChatRoom savedRoom = captor.getValue();
        assertEquals(0L, savedRoom.getSetAmount()); // null이면 0으로 세팅 확인

        verify(memberRepo).save(any(ChatMember.class));
    }


    @Test
    @DisplayName("채팅방 생성 - 존재하지 않는 유저이면 예외 발생")
    void createRoom_userNotFound_throwsException() {
        ChatRoomRequestDTO dto = ChatRoomRequestDTO.builder()
                .roomName("테스트방")
                .setAmount(5L)
                .build();
        Long userId = 999L;

        when(userRepo.findById(userId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            service.createRoom(dto, userId);
        });
    }

    @Test
    @DisplayName("채팅방이 존재하지 않으면 NoSuchElementException 발생")
    void enterRoom_whenChatRoomNotFound_thenThrowException() {
        // given
        Long roomId = 1L;
        Long userId = 1L;
        given(roomRepo.findById(roomId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> service.enterRoom(roomId, userId))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("채팅방이 존재하지 않습니다.");
    }

    @Test
    @DisplayName("유저 연봉이 null이면 ResponseStatusException 발생 (미등록)")
    void enterRoom_whenUserSalaryIsNull_thenThrowForbidden() {
        // given
        Long roomId = 1L;
        Long userId = 1L;

        ChatRoom room = new ChatRoom();
        room.setSetAmount(5000L);

        User user = new User();
        user.setSalary(null);

        given(roomRepo.findById(roomId)).willReturn(Optional.of(room));
        given(userRepo.findById(userId)).willReturn(Optional.of(user));

        // when & then
        assertThatThrownBy(() -> service.enterRoom(roomId, userId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("미등록");
    }

    @Test
    @DisplayName("유저 연봉이 최소 연봉보다 낮으면 ResponseStatusException 발생")
    void enterRoom_whenUserSalaryLessThanMin_thenThrowForbidden() {
        // given
        Long roomId = 1L;
        Long userId = 1L;

        ChatRoom room = new ChatRoom();
        room.setSetAmount(5000L);

        User user = new User();
        user.setSalary(3000L);

        given(roomRepo.findById(roomId)).willReturn(Optional.of(room));
        given(userRepo.findById(userId)).willReturn(Optional.of(user));

        // when & then
        assertThatThrownBy(() -> service.enterRoom(roomId, userId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("최소 연봉 5000 이상만 입장할 수 있습니다. (현재 연봉: 3000)");
    }

    @Test
    @DisplayName("유저가 속한 채팅방이 아예 없을 때")
    void returnsEmptyPageWhenUserHasNoChatRooms() {
        // given
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        Page<ChatRoom> emptyPage = Page.empty(pageable);

        given(memberRepo.findChatRoomsByUserId(userId, pageable))
                .willReturn(emptyPage);

        // when
        Page<UserRoomResponseDTO> result = service.userRoomList(userId, 0, 10);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();

        // chatMessageRepository는 호출되지 않아야 함
        then(chatMessageRepository).should(never())
                .countUnreadByUserAndRoomIds(anyLong(), anyList());
    }

    @Test
    @DisplayName("검색 키워드가 있을 때 findByRoomNameContainingIgnoreCase 호출")
    void testRoomListWithKeyword() {
        // 테스트 데이터 생성 (빌더 사용)
        ChatRoom room1 = ChatRoom.builder()
                .id(1L)
                .roomName("Java Room")
                .build();

        ChatRoom room2 = ChatRoom.builder()
                .id(2L)
                .roomName("Spring Boot Room")
                .build();

        Pageable pageable = PageRequest.of(0, 10);

        // Repository Mock 설정
        Page<ChatRoom> mockPage = new PageImpl<>(List.of(room1, room2));
        when(roomRepo.findByRoomNameContainingIgnoreCase("java", pageable))
                .thenReturn(mockPage);

        // 실행
        Page<ChatRoomResponseDTO> result = service.roomList("java", pageable);

        // 검증
        verify(roomRepo).findByRoomNameContainingIgnoreCase("java", pageable);
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getRoomName()).isEqualTo("Java Room");
    }

    @Test
    @DisplayName("검색 키워드가 없을 때 findAll 호출")
    void testRoomListWithoutKeyword() {
        // 테스트 데이터 생성 (빌더 사용)
        ChatRoom room1 = ChatRoom.builder()
                .id(1L)
                .roomName("Java Room")
                .build();

        ChatRoom room2 = ChatRoom.builder()
                .id(2L)
                .roomName("Spring Boot Room")
                .build();

        Pageable pageable = PageRequest.of(0, 10);

        // Repository Mock 설정
        Page<ChatRoom> mockPage = new PageImpl<>(List.of(room1, room2));
        when(roomRepo.findAll(pageable)).thenReturn(mockPage);

        // 실행
        Page<ChatRoomResponseDTO> result = service.roomList(null, pageable);

        // 검증
        verify(roomRepo).findAll(pageable);
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(1).getRoomName()).isEqualTo("Spring Boot Room");
    }

    @Test
    @DisplayName("검색 키워드가 빈 문자열일 때 findAll 호출")
    void testRoomListWithEmptyKeyword() {
        Pageable pageable = PageRequest.of(0, 10);
        ChatRoom room1 = ChatRoom.builder().id(1L).roomName("Java Room").build();
        ChatRoom room2 = ChatRoom.builder().id(2L).roomName("Spring Boot Room").build();
        Page<ChatRoom> mockPage = new PageImpl<>(List.of(room1, room2));

        when(roomRepo.findAll(pageable)).thenReturn(mockPage);

        Page<ChatRoomResponseDTO> result = service.roomList("", pageable);

        verify(roomRepo).findAll(pageable);
        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("유저가 방에 있을 경우 정상적으로 나가기")
    void testRoomLeaveSuccess() {
        Long userId = 1L;
        Long roomId = 100L;
        ChatMemberId memberId = new ChatMemberId(userId, roomId);

        ChatMember member = ChatMember.builder()
                .id(memberId)
                .build();

        when(memberRepo.findById(any(ChatMemberId.class)))
                .thenReturn(Optional.of(member));

        service.roomLeave(roomId, userId);

        verify(memberRepo).delete(member);
    }
}
