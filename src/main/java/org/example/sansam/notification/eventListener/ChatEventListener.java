package org.example.sansam.notification.eventListener;

import lombok.AllArgsConstructor;
import org.example.sansam.chat.domain.ChatMember;
import org.example.sansam.chat.repository.ChatMemberRepository;
import org.example.sansam.notification.event.ChatEvent;
import org.example.sansam.notification.service.NotificationService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@AllArgsConstructor
public class ChatEventListener {
    private final NotificationService notificationService;
    private final ChatMemberRepository chatMemberRepository;

    @EventListener
    public void handleChatEvent(ChatEvent event) {

        Optional<List<ChatMember>> chatMemberList = chatMemberRepository.findAllByChatRoomId(event.getChatRoom().getId());

        chatMemberList.ifPresent(list ->{
            list.forEach(chatMember -> {
                if (!chatMember.getUser().getId().equals(event.getUser().getId())){
                    notificationService.sendChatNotification(chatMember.getUser(), chatMember.getChatRoom().getRoomName(), event.getMessage());
                }
            });
        });
    }
}
