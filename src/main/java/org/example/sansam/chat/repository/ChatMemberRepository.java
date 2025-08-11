package org.example.sansam.chat.repository;

import org.example.sansam.chat.domain.ChatMember;
import org.example.sansam.chat.domain.ChatMemberId;
import org.example.sansam.chat.domain.ChatRoom;
import org.example.sansam.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ChatMemberRepository extends JpaRepository<ChatMember, ChatMemberId> {

    @Query("SELECT cm.chatRoom FROM ChatMember cm WHERE cm.user.id = :userId ORDER BY cm.chatRoom.lastMessageAt DESC")
    Page<ChatRoom> findChatRoomsByUserId(Long userId, Pageable pageable);

    @Modifying
    @Query("""
        UPDATE ChatMember cm
        SET cm.lastReadAt = :ts
        WHERE cm.id.userId = :userId
          AND cm.id.chatRoomId = :roomId
        """)
    void updateLastReadAt(@Param("userId") Long userId,
                          @Param("roomId") Long roomId,
                          @Param("ts")     LocalDateTime ts);

    boolean existsByUserIdAndChatRoomId(Long userId, Long roomId);

    Optional<List<ChatMember>> findAllByChatRoomId(Long roomId);
}