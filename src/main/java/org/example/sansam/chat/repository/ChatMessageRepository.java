package org.example.sansam.chat.repository;

import org.example.sansam.chat.domain.ChatMessage;
import org.example.sansam.chat.domain.ChatRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findTopByChatRoomOrderByCreatedAtDesc(ChatRoom chatRoom, Pageable pageable);

    List<ChatMessage> findByChatRoomAndIdLessThanOrderByCreatedAtDesc(ChatRoom chatRoom, Long lastMessageId, Pageable pageable);
}
