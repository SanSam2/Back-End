package org.example.sansam.chat.service;

import lombok.RequiredArgsConstructor;
import org.example.sansam.chat.domain.ChatMember;
import org.example.sansam.chat.domain.ChatMemberId;
import org.example.sansam.chat.domain.ChatRoom;
import org.example.sansam.chat.dto.ChatRoomRequestDTO;
import org.example.sansam.chat.dto.ChatRoomResponseDTO;
import org.example.sansam.chat.dto.RoomCountDTO;
import org.example.sansam.chat.dto.UserRoomResponseDTO;
import org.example.sansam.chat.repository.ChatMemberRepository;
import org.example.sansam.chat.repository.ChatMessageRepository;
import org.example.sansam.chat.repository.ChatRoomRepository;
import org.example.sansam.user.domain.User;
import org.example.sansam.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMemberRepository chatMemberRepository;
    private final UserRepository userRepository;
    private final ChatMessageRepository chatMessageRepository;

    // 유저가 입장하고 있는 방 리스트
    @Transactional(readOnly = true)
    public Page<UserRoomResponseDTO> userRoomList(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size,
                Sort.by("lastMessageAt").descending());

        // 1) 이 유저가 속한 Room 목록 조회
        Page<ChatRoom> chatRooms =
                chatMemberRepository.findChatRoomsByUserId(userId, pageable);

        // 2) roomId 리스트 뽑기
        List<Long> roomIds = chatRooms.stream()
                .map(ChatRoom::getId)
                .toList();

        if (roomIds.isEmpty()) {
            return chatRooms.map(cr ->
                    UserRoomResponseDTO.fromEntity(cr, 0L));
        }

        List<RoomCountDTO> counts =
                chatMessageRepository.countUnreadByUserAndRoomIds(userId, roomIds);

        Map<Long, Long> unreadMap = counts.stream()
                .collect(Collectors.toMap(RoomCountDTO::getRoomId,
                        RoomCountDTO::getCount));

        return chatRooms.map(cr -> {
            Long unread = unreadMap.getOrDefault(cr.getId(), 0L);
            return UserRoomResponseDTO.fromEntity(cr, unread);
        });
    }


    // 채팅방 리스트 조회 (검색)
    @Transactional(readOnly = true)
    public Page<ChatRoomResponseDTO> roomList(String roomName, Pageable pageable) {
        Page<ChatRoom> rooms;
        if (roomName != null && !roomName.trim().isEmpty()) {
            rooms = chatRoomRepository.findByRoomNameContainingIgnoreCase(roomName.trim(), pageable);
        } else {
            rooms = chatRoomRepository.findAll(pageable);
        }
        return rooms.map(ChatRoomResponseDTO::fromEntity);
    }

    // 채팅방 생성
    @Transactional
    public ChatRoomResponseDTO createRoom(ChatRoomRequestDTO dto, Long userId) {

        ChatRoom chatRoom = ChatRoom.builder()
                .roomName(dto.getRoomName())
                .createdAt(LocalDateTime.now())
                .lastMessageAt(LocalDateTime.now())
                .setAmount(dto.getSetAmount() == null ? 0 : dto.getSetAmount())
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

        Long userSalary = user.getSalary();
        Long minSalary  = room.getSetAmount();

        if (userSalary == null || userSalary < minSalary) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "죄송합니다. 최소 연봉 " + minSalary + " 이상만 입장할 수 있습니다. (현재 연봉: "
                            + (userSalary == null ? "미등록" : userSalary) + ")");
        }

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