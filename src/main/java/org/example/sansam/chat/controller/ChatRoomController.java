package org.example.sansam.chat.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.example.sansam.chat.dto.ChatRoomRequestDTO;
import org.example.sansam.chat.dto.ChatRoomResponseDTO;
import org.example.sansam.chat.dto.UserRoomResponseDTO;
import org.example.sansam.chat.service.ChatRoomService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/chatroom")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;


    // 입장한 채팅방 목록
    @GetMapping("/user")
    public ResponseEntity<?> UserChatRoomGet(HttpSession session,
                                             @RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "20") int size) {

        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }

            Page<UserRoomResponseDTO> rooms = chatRoomService.userRoomList(userId, page, size);
            return ResponseEntity.ok(rooms);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    // 채팅방 검색 및 전체조회
    @GetMapping
    public ResponseEntity<?> chatroom(@RequestParam(required = false) String keyword,
                                      @RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ChatRoomResponseDTO> chatRoomResponseDTO = chatRoomService.roomList(keyword, pageable);
        return new ResponseEntity<>(chatRoomResponseDTO, HttpStatus.OK);
    }

    // 채팅방 생성
    @PostMapping
    public ResponseEntity<?> createRoom(@RequestBody ChatRoomRequestDTO chatRoomRequestDTO, HttpSession session){

        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }

            ChatRoomResponseDTO chatRoomResponseDTO = chatRoomService.createRoom(chatRoomRequestDTO, userId);
            return  new ResponseEntity<>(chatRoomResponseDTO, HttpStatus.OK);
        } catch (Exception e){
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    // 채팅방입장
    @PostMapping("/{roomId}/enter")
    public ResponseEntity<?> chatroomEnter(@PathVariable Long roomId, HttpSession session) {
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }

            ChatRoomResponseDTO responseDTO = chatRoomService.enterRoom(roomId, userId);

            return ResponseEntity.ok(responseDTO);
        } catch (ResponseStatusException amount_ero) {
            throw amount_ero;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("채팅방 입장 중 오류가 발생했습니다.");
        }
    }


    // 채팅방 퇴장
    @DeleteMapping("/{roomId}/leave")
    public ResponseEntity<?> chatroomLeave(@PathVariable Long roomId, HttpSession session) {

        try{
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }

            chatRoomService.roomLeave(roomId, userId);
            return ResponseEntity.noContent().build();
        }catch (Exception e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
