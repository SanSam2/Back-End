package org.example.sansam.notification.eventListener.sse;

import org.example.sansam.chat.domain.ChatMember;
import org.example.sansam.chat.domain.ChatRoom;
import org.example.sansam.chat.repository.ChatMemberRepository;
import org.example.sansam.notification.event.sse.ChatEvent;
import org.example.sansam.notification.service.NotificationService;
import org.example.sansam.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class ChatEventListenerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private ChatMemberRepository chatMemberRepository;

    @InjectMocks
    private ChatEventListener chatEventListener;

    @DisplayName("채팅 이벤트 발생 시 발신자 제외 모든 멤버에게 알림을 보낸다")
    @Test
    void sendNotificationToAllMembersExceptSender(){
        // given
        User sender = User.builder().id(1L).name("보낸사람").build();
        User receiver1 = User.builder().id(2L).name("받는사람1").build();
        User receiver2 = User.builder().id(3L).name("받는사람2").build();
        ChatRoom chatRoom = ChatRoom.builder().id(100L).roomName("테스트방").build();

        ChatMember memberSender = ChatMember.builder().user(sender).chatRoom(chatRoom).build();
        ChatMember memberReceiver1 = ChatMember.builder().user(receiver1).chatRoom(chatRoom).build();
        ChatMember memberReceiver2 = ChatMember.builder().user(receiver2).chatRoom(chatRoom).build();

        ChatEvent event = new ChatEvent(chatRoom, sender, "안녕");

        when(chatMemberRepository.findAllByChatRoomId(chatRoom.getId()))
                .thenReturn(Optional.of(List.of(memberSender, memberReceiver1, memberReceiver2)));

        // when
        chatEventListener.handleChatEvent(event);

        // then
        verify(notificationService, times(1))
                .sendChatNotification(receiver1, chatRoom.getRoomName(),"안녕");
        verify(notificationService, times(1))
                .sendChatNotification(receiver2, chatRoom.getRoomName(),"안녕");
        verify(notificationService, never())
                .sendChatNotification(sender, chatRoom.getRoomName(),"안녕");
    }

    @DisplayName("ChatMember의 User가 null 이면 알림을 보내지 않는다.")
    @Test
    void handleChatEvent_when_member_user_is_null_then_not_send_notification(){
        // given
        User sender = User.builder().id(1L).name("보낸사람").build();
        ChatRoom chatRoom = ChatRoom.builder().id(100L).roomName("테스트방").build();

        ChatMember invalidMember = ChatMember.builder().chatRoom(chatRoom).user(null).build();
        ChatEvent event = new ChatEvent(chatRoom, sender, "메시지");

        when(chatMemberRepository.findAllByChatRoomId(chatRoom.getId()))
                .thenReturn(Optional.of(List.of(invalidMember)));
        // when
        chatEventListener.handleChatEvent(event);

        // then
        verify(notificationService, never()).sendChatNotification(any(), any(), any());
    }

    @DisplayName("이벤트 User가 null 이면 알림을 보내지 않는다.")
    @Test
    void handleChatEvent_when_event_user_is_null_then_not_send_notification(){
        // given
        User receiver = User.builder().id(2L).name("받는사람").build();
        ChatRoom chatRoom = ChatRoom.builder().id(100L).roomName("테스트방").build();

        ChatMember member = ChatMember.builder().chatRoom(chatRoom).user(receiver).build();
        ChatEvent event = new ChatEvent(chatRoom, null, "메시지");

        when(chatMemberRepository.findAllByChatRoomId(chatRoom.getId()))
                .thenReturn(Optional.of(List.of(member)));

        // when
        chatEventListener.handleChatEvent(event);

        // then
        verify(notificationService, never()).sendChatNotification(any(), any(), any());
    }

    @DisplayName("알림 전송 중 예외가 발생해도 예외는 전파되지 않는다")
    @Test
    void handleChatEvent_when_notificationService_throws_exception(){
        // given
        User sender = User.builder().id(1L).name("보낸사람").build();
        User receiver = User.builder().id(2L).name("받는사람").build();
        ChatRoom chatRoom = ChatRoom.builder().id(100L).roomName("테스트방").build();

        ChatMember member = ChatMember.builder().chatRoom(chatRoom).user(receiver).build();
        ChatEvent event = new ChatEvent(chatRoom, sender, "메시지");

        when(chatMemberRepository.findAllByChatRoomId(chatRoom.getId()))
                .thenReturn(Optional.of(List.of(member)));

        doThrow(new RuntimeException("푸시 실패"))
                .when(notificationService)
                .sendChatNotification(any(), any(), any());

        // when
        chatEventListener.handleChatEvent(event);

        // then (예외 전파 안 됨)
        verify(notificationService, times(1)).sendChatNotification(receiver, chatRoom.getRoomName(), "메시지");
    }

    @DisplayName("채팅방 멤버가 없으면 아무 동작도 하지 않는다")
    @Test
    void handleChatEvent_when_no_chat_members(){
        // given
        User sender = User.builder().id(1L).name("보낸사람").build();
        ChatRoom chatRoom = ChatRoom.builder().id(100L).roomName("테스트방").build();
        ChatEvent event = new ChatEvent(chatRoom, sender, "메시지");

        when(chatMemberRepository.findAllByChatRoomId(chatRoom.getId()))
                .thenReturn(Optional.empty());

        // when
        chatEventListener.handleChatEvent(event);

        // then
        verify(notificationService, never()).sendChatNotification(any(), any(), any());
    }
}