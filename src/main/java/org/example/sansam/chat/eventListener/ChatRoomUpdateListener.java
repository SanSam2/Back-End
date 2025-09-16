package org.example.sansam.chat.eventListener;

import org.example.sansam.chat.event.ChatRoomUpdateEvent;
import org.example.sansam.chat.repository.ChatRoomRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class ChatRoomUpdateListener {

    private final ChatRoomRepository chatRoomRepository;

    public ChatRoomUpdateListener(ChatRoomRepository chatRoomRepository) {
        this.chatRoomRepository = chatRoomRepository;
    }

    @Async("chatTaskExecutor")
    @TransactionalEventListener
    public void handleChatRoomUpdate(ChatRoomUpdateEvent event) {
        chatRoomRepository.findById(event.getRoomId()).ifPresent(chatRoom -> {
            chatRoom.setLastMessageAt(event.getLastMessageAt());
            chatRoomRepository.save(chatRoom);
        });
    }
}