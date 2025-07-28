package org.example.sansam.chat.service;

import jakarta.servlet.http.HttpSession;
import org.example.sansam.chat.dto.ChatMessageRequestDTO;
import org.example.sansam.chat.dto.ChatMessageResponseDTO;
import org.example.sansam.chat.dto.ChatRoomResponseDTO;
import org.example.sansam.chat.repository.ChatMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatMessageService {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    public List<ChatMessageResponseDTO> getMessages(String roomId) {
        return null;
    }

    public void deleteMessage(String messageId, HttpSession session) {
    }

    public ChatRoomResponseDTO addMessage(String roomId, ChatMessageRequestDTO chatMessageRequestDTO,
                                          HttpSession session) {
        return null;
    }
}
