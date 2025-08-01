package org.example.sansam.chat.repository;

import org.example.sansam.chat.domain.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    List<ChatRoom> findByRoomNameContainingIgnoreCase(String roomName);
}
