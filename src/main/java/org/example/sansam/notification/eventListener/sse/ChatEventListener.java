package org.example.sansam.notification.eventListener.sse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.sansam.chat.domain.ChatMember;
import org.example.sansam.chat.repository.ChatMemberRepository;
import org.example.sansam.notification.event.sse.ChatEvent;
import org.example.sansam.notification.service.NotificationService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatEventListener {
    private final NotificationService notificationService;
    private final ChatMemberRepository chatMemberRepository;

    @EventListener
    public void handleChatEvent(ChatEvent event) {

        Optional<List<ChatMember>> chatMemberList = chatMemberRepository.findAllByChatRoomId(event.getChatRoom().getId());

        chatMemberList.ifPresent(list -> {
            list.forEach(chatMember -> {
                try {
                    if (chatMember.getUser() == null) {
                        log.warn("ChatMember의 User가 null입니다. chatMember: {}", chatMember);
                        return;
                    }
                    if (event.getUser() == null) {
                        log.warn("이벤트의 User가 null입니다. event: {}", event);
                        return;
                    }

                    Long chatMemberUserId = chatMember.getUser().getId();
                    Long eventUserId = event.getUser().getId();

                    if (chatMemberUserId != null && !chatMemberUserId.equals(eventUserId)) {
                        notificationService.sendChatNotification(chatMember.getUser(), chatMember.getChatRoom().getRoomName(), event.getMessage());
                    }
                } catch (Exception e) {
                    log.error("채팅 알림 전송 실패 - chatMemberUserId={}", chatMember.getUser() != null ? chatMember.getUser().getId() : "null", e);
                }
            });
        });
    }
}
