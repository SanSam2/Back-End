package org.example.sansam.chat.service;

import lombok.RequiredArgsConstructor;
import org.example.sansam.chat.domain.ChatMessage;
import org.example.sansam.chat.domain.ChatRoom;
import org.example.sansam.chat.dto.ChatMessageRequestDTO;
import org.example.sansam.chat.dto.ChatMessageResponseDTO;

import org.example.sansam.chat.repository.ChatMemberRepository;
import org.example.sansam.chat.repository.ChatMessageRepository;
import org.example.sansam.chat.repository.ChatRoomRepository;
import org.example.sansam.user.domain.User;
import org.example.sansam.user.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    private final ChatMemberRepository chatMemberRepository;

    // 채팅방 메세지 페이징 처리
    @Transactional(readOnly = true)
    public List<ChatMessageResponseDTO> getMessages(Long roomId, Long lastMessageId, Long userId , int size) {

        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방입니다."));

        boolean isMember = chatMemberRepository.existsByUserIdAndChatRoomId(userId, roomId);

        if (!isMember) {
            throw new SecurityException("채팅방에 접근할 권한이 없습니다.");
        }


        Pageable pageable = PageRequest.of(0, size);

        List<ChatMessage> messages;
        if (lastMessageId == null) {
            messages = chatMessageRepository.findTopByChatRoomOrderByCreatedAtDesc(chatRoom, pageable);
        } else {
            messages = chatMessageRepository.findByChatRoomAndIdLessThanOrderByCreatedAtDesc(chatRoom, lastMessageId, pageable);
        }

        List<ChatMessageResponseDTO> chatMessageResponseDTOS = messages.stream()
                .map(msg -> ChatMessageResponseDTO.fromEntity(msg, msg.getSender().getName(), roomId))
                .collect(Collectors.toList());
        Collections.reverse(chatMessageResponseDTOS);

        return chatMessageResponseDTOS;
    }

    // 메세지 전송시, 데이터 베이스에 추가
    @Transactional
    public ChatMessageResponseDTO addMessage(ChatMessageRequestDTO chatMessageRequestDTO, Long userId, Long roomId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("존재하지 않는 유저입니다. "));

        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다."));

        ChatMessage chatMessage = ChatMessage.builder()
                .chatRoom(chatRoom)
                .sender(user)
                .message(chatMessageRequestDTO.getMessage())
                .createdAt(LocalDateTime.now())
                .build();

        chatMessageRepository.save(chatMessage);
        chatRoom.setLastMessageAt(chatMessage.getCreatedAt());

        return ChatMessageResponseDTO.fromEntity(chatMessage,user.getName(), roomId);
    }

    // 메세지 삭제
    @Transactional
    public void deleteMessage(Long messageId, Long userId) {

        ChatMessage chatMessage = chatMessageRepository.findById(messageId)
                .orElseThrow(() ->
                        new IllegalArgumentException("존재하지 않는 메세지입니다."));

        if (!chatMessage.getSender().getId().equals(userId)) {
            throw new IllegalArgumentException("본인의 메시지만 삭제할 수 있습니다.");
        }

        chatMessageRepository.delete(chatMessage);
    }

}
