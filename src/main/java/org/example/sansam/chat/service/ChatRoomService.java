package org.example.sansam.chat.service;

import lombok.RequiredArgsConstructor;
import org.example.sansam.chat.domain.ChatMember;
import org.example.sansam.chat.domain.ChatMemberId;
import org.example.sansam.chat.domain.ChatRoom;
import org.example.sansam.chat.dto.ChatRoomRequestDTO;
import org.example.sansam.chat.dto.ChatRoomResponseDTO;
import org.example.sansam.chat.repository.ChatMemberRepository;
import org.example.sansam.chat.repository.ChatRoomRepository;
import org.example.sansam.user.domain.User;
import org.example.sansam.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMemberRepository chatMemberRepository;
    private final UserRepository userRepository;

    // 유저가 입장하고 있는 방 리스트
    @Transactional(readOnly = true)
    public List<ChatRoomResponseDTO> userRoomList(Long userId, int page, int size) {

        List<ChatRoom> rooms = chatMemberRepository
                .findAllByIdUserId(userId)
                .stream()
                .map(ChatMember::getChatRoom)
                .distinct()
                .sorted(Comparator.comparing(ChatRoom::getLastMessageAt).reversed())
                .toList();

        return rooms.stream()
                .skip((long) page * size)
                .limit(size)
                .map(ChatRoomResponseDTO::fromEntity)
                .toList();
    }

    // 채팅방 리스트 조회 (검색)
    public List<ChatRoomResponseDTO> roomList(String roomName) {
        List<ChatRoom> rooms;
        if (roomName != null && !roomName.trim().isEmpty()) {
            rooms = chatRoomRepository.findByRoomNameContainingIgnoreCase((roomName.trim()));
        } else {
            rooms = chatRoomRepository.findAll();
        }
        return rooms.stream()
                .map(ChatRoomResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // 채팅방 생성
    @Transactional
    public ChatRoomResponseDTO createRoom(ChatRoomRequestDTO dto, Long userId) {

        ChatRoom chatRoom = ChatRoom.builder()
                .roomName(dto.getRoomName())
                .createdAt(LocalDateTime.now())
                .lastMessageAt(LocalDateTime.now())
                .build();

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("존재하지 않는 유저입니다. "));

        chatRoom = chatRoomRepository.save(chatRoom);

        ChatMemberId memberId = new ChatMemberId(userId, chatRoom.getId());

        ChatMember member = ChatMember.builder()
                .id(memberId)
                .user(user)
                .chatRoom(chatRoom)
                .build();
        chatMemberRepository.save(member);

        return ChatRoomResponseDTO.fromEntity(chatRoom);
    }

    //채팅방 입장
    @Transactional
    public ChatRoomResponseDTO enterRoom(Long roomId, Long userId) {

        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new NoSuchElementException("채팅방이 존재하지 않습니다."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("유저가 존재하지 않습니다."));

        ChatMemberId id = new ChatMemberId(userId, roomId);
        boolean already = chatMemberRepository.existsById(id);

        if (!already) {
            ChatMember member = ChatMember.builder()
                    .id(id)
                    .user(user)
                    .chatRoom(room)
                    .build();
            chatMemberRepository.save(member);
        }

        return ChatRoomResponseDTO.fromEntity(room);

    }

    // 채팅방 퇴장
    @Transactional
    public void roomLeave(Long roomId, Long userId) {

        ChatMemberId memberId = new ChatMemberId(userId, roomId);

        ChatMember member = chatMemberRepository.findById(memberId)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "유저가 방의 멤버가 아닙니다.")
                );

        chatMemberRepository.delete(member);
    }
}