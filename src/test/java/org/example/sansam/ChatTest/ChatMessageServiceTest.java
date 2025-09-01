package org.example.sansam.ChatTest;

import org.example.sansam.chat.domain.ChatMessage;
import org.example.sansam.chat.domain.ChatRoom;
import org.example.sansam.chat.dto.ChatMessageRequestDTO;
import org.example.sansam.chat.dto.ChatMessageResponseDTO;
import org.example.sansam.chat.repository.ChatMemberRepository;
import org.example.sansam.chat.repository.ChatMessageRepository;
import org.example.sansam.chat.repository.ChatRoomRepository;
import org.example.sansam.chat.service.ChatMessageService;
import org.example.sansam.user.domain.User;
import org.example.sansam.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChatMessageServiceTest {

    @InjectMocks
    private ChatMessageService chatMessageService;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ChatMemberRepository chatMemberRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // 메시지 조회 테스트
    @Test
    @DisplayName("채팅방 메시지 조회 시, 멤버가 아니면 SecurityException 발생")
    void getMessages_notMember_throwsException() {
        Long roomId = 1L;
        Long userId = 1L;

        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setId(roomId);

        when(chatRoomRepository.findById(roomId)).thenReturn(Optional.of(chatRoom));
        when(chatMemberRepository.existsByUserIdAndChatRoomId(userId, roomId)).thenReturn(false);

        assertThrows(SecurityException.class,
                () -> chatMessageService.getMessages(roomId, null, userId, 10));
    }

    @Test
    @DisplayName("채팅방 메시지 조회 시, 멤버이면 메시지를 정상 반환")
    void getMessages_member_returnsMessages() {
        Long roomId = 1L;
        Long userId = 1L;

        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setId(roomId);

        User user = new User();
        user.setId(userId);

        // 멤버 확인
        when(chatRoomRepository.findById(roomId)).thenReturn(Optional.of(chatRoom));
        when(chatMemberRepository.existsByUserIdAndChatRoomId(userId, roomId)).thenReturn(true);

        // Pageable 준비
        int size = 10;
        Pageable pageable = PageRequest.of(0, size);

        // 반환할 메시지 리스트 준비
        List<ChatMessage> messages = List.of(
                ChatMessage.builder()
                        .id(1L)
                        .chatRoom(chatRoom)
                        .sender(user)
                        .message("첫 메시지")
                        .createdAt(LocalDateTime.now())
                        .build(),
                ChatMessage.builder()
                        .id(2L)
                        .chatRoom(chatRoom)
                        .sender(user)
                        .message("두 번째 메시지")
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        // Mockito 설정: findMessagesWithSender 모킹
        when(chatMessageRepository.findMessagesWithSender(chatRoom, null, pageable))
                .thenReturn(messages);

        // 서비스 호출
        Page<ChatMessageResponseDTO> result = chatMessageService.getMessages(roomId, null, userId, size);

        // 검증
        assertNotNull(result);
        assertEquals(2, result.getContent().size());

        // 서비스에서는 역순으로 변환하므로 첫 번째 메시지가 마지막이 됨
        assertEquals("첫 메시지", result.getContent().get(1).getMessage());
        assertEquals("두 번째 메시지", result.getContent().get(0).getMessage());

        verify(chatMessageRepository).findMessagesWithSender(chatRoom, null, pageable);
    }

    @Test
    @DisplayName("채팅방 메시지 조회 시, lastMessageId가 있으면 이전 메시지를 정상 반환")
    void getMessages_withLastMessageId_returnsPreviousMessages() {
        Long roomId = 1L;
        Long userId = 1L;
        Long lastMessageId = 2L; // 이전 메시지 기준 ID

        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setId(roomId);

        User user = new User();
        user.setId(userId);

        // 멤버 확인
        when(chatRoomRepository.findById(roomId)).thenReturn(Optional.of(chatRoom));
        when(chatMemberRepository.existsByUserIdAndChatRoomId(userId, roomId)).thenReturn(true);

        // Pageable 준비
        int size = 10;
        Pageable pageable = PageRequest.of(0, size);

        // 반환할 메시지 리스트 준비 (lastMessageId보다 작은 메시지)
        List<ChatMessage> messages = List.of(
                ChatMessage.builder()
                        .id(1L)
                        .chatRoom(chatRoom)
                        .sender(user)
                        .message("첫 메시지")
                        .createdAt(LocalDateTime.now().minusMinutes(2))
                        .build()
        );

        // Mockito 설정: findMessagesWithSender 모킹
        when(chatMessageRepository.findMessagesWithSender(chatRoom, lastMessageId, pageable))
                .thenReturn(messages);

        // 서비스 호출
        Page<ChatMessageResponseDTO> result = chatMessageService.getMessages(roomId, lastMessageId, userId, size);

        // 검증
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("첫 메시지", result.getContent().get(0).getMessage());

        verify(chatMessageRepository).findMessagesWithSender(chatRoom, lastMessageId, pageable);
    }

    // 메시지 전송 테스트
    @Test
    @DisplayName("존재하지 않는 유저일 경우 addMessage 예외 발생")
    void addMessage_nonExistentUser_throwsException() {
        Long userId = 1L;
        Long roomId = 1L;
        ChatMessageRequestDTO dto = new ChatMessageRequestDTO();
        dto.setMessage("테스트 메시지");

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> chatMessageService.addMessage(dto, userId, roomId));
    }

    // 메시지 삭제 테스트
    @Test
    @DisplayName("본인이 아닌 메시지를 삭제하면 예외 발생")
    void deleteMessage_notSender_throwsException() {
        Long messageId = 1L;
        Long roomId = 1L;
        Long userId = 2L; // 실제 sender는 1L

        ChatMessage message = ChatMessage.builder()
                .id(messageId)
                .chatRoom(ChatRoom.builder().id(roomId).build())
                .sender(User.builder().id(1L).build())
                .message("테스트")
                .createdAt(LocalDateTime.now())
                .build();

        when(chatMessageRepository.findById(messageId)).thenReturn(Optional.of(message));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> chatMessageService.deleteMessage(messageId, userId, roomId));
        assertEquals("본인의 메시지만 삭제할 수 있습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("deleteMessage: 메시지가 존재하지 않으면 IllegalArgumentException 발생")
    void deleteMessage_MessageNotExist() {
        Long senderId = 1L;
        Long roomId = 1L;
        Long nonExistentId = 999L;

        when(chatMessageRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                chatMessageService.deleteMessage(nonExistentId, senderId, roomId));

        assertEquals("존재하지 않는 메세지입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("deleteMessage: 잘못된 방의 메시지 삭제 시 IllegalArgumentException 발생")
    void deleteMessage_WrongRoom() {
        Long senderId = 1L;
        Long roomId = 1L;
        Long anotherRoomId = 2L;
        Long messageId = 10L;

        User sender = User.builder().id(senderId).build();
        ChatRoom room = ChatRoom.builder().id(roomId).build();

        ChatMessage message = ChatMessage.builder()
                .id(messageId)
                .sender(sender)
                .chatRoom(room)
                .message("테스트 메시지")
                .build();

        when(chatMessageRepository.findById(messageId)).thenReturn(Optional.of(message));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                chatMessageService.deleteMessage(messageId, senderId, anotherRoomId));

        assertEquals(
                String.format("잘못된 접근입니다. 메시지[%d]는 방[%d]의 메시지가 아닙니다.", messageId, anotherRoomId),
                exception.getMessage()
        );
    }

    @Test
    @DisplayName("deleteMessage: 올바른 경우 메시지 삭제 성공")
    void deleteMessage_Success() {
        Long senderId = 1L;
        Long roomId = 1L;
        Long messageId = 10L;

        User sender = User.builder().id(senderId).build();
        ChatRoom room = ChatRoom.builder().id(roomId).build();

        ChatMessage message = ChatMessage.builder()
                .id(messageId)
                .sender(sender)
                .chatRoom(room)
                .message("테스트 메시지")
                .build();

        when(chatMessageRepository.findById(messageId)).thenReturn(Optional.of(message));

        // 실제 삭제 동작은 Mockito verify로 확인
        doNothing().when(chatMessageRepository).delete(message);

        chatMessageService.deleteMessage(messageId, senderId, roomId);

        verify(chatMessageRepository, times(1)).delete(message);
    }
}
