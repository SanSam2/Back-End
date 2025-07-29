package org.example.sansam.chat.service;

import jakarta.servlet.http.HttpSession;
import org.example.sansam.chat.dto.ChatRoomResponseDTO;
import org.example.sansam.chat.repository.ChatRoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatRoomService {

    @Autowired
    private ChatRoomRepository chatRoomRepository;


    public ChatRoomResponseDTO roomConnection(String roomId, HttpSession session) {
        return null;
    }

    public List<ChatRoomResponseDTO> roomList() {
        return null;
    }

    public ChatRoomResponseDTO roomLeave(String roomId, HttpSession session) {return  null;}
}