package org.example.sansam.chat.repository;

import org.example.sansam.chat.domain.ChatRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    Page<ChatRoom> findByRoomNameContainingIgnoreCase(String keyword, Pageable pageable);

    Optional<ChatRoom> findByRoomName(String 테스트방);
}