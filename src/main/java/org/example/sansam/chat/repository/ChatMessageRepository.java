package org.example.sansam.chat.repository;

import org.example.sansam.chat.domain.ChatMessage;
import org.example.sansam.chat.domain.ChatRoom;
import org.example.sansam.chat.dto.RoomCountDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findTopByChatRoomOrderByCreatedAtDesc(ChatRoom chatRoom, Pageable pageable);

    List<ChatMessage> findByChatRoomAndIdLessThanOrderByCreatedAtDesc(ChatRoom chatRoom, Long lastMessageId, Pageable pageable);

    @Query("""
      select new org.example.sansam.chat.dto.RoomCountDTO(
               m.chatRoom.id,
               count(m)
             )
      from ChatMessage m
      join ChatMember cm
        on cm.chatRoom.id = m.chatRoom.id
       and cm.user.id = :userId
      where m.chatRoom.id in :roomIds
        and (cm.lastReadAt is null or m.createdAt > cm.lastReadAt)
      group by m.chatRoom.id
    """)
    List<RoomCountDTO> countUnreadByUserAndRoomIds(
            @Param("userId") Long userId,
            @Param("roomIds") List<Long> roomIds
    );}
