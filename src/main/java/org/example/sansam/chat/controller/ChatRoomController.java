package org.example.sansam.chat.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.example.sansam.chat.dto.ChatRoomRequestDTO;
import org.example.sansam.chat.dto.ChatRoomResponseDTO;
import org.example.sansam.chat.service.ChatRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

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

            List<ChatRoomResponseDTO> rooms = chatRoomService.userRoomList(userId, page, size);
            return ResponseEntity.ok(rooms);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    // 채팅방 전체조회 , 검색
    @GetMapping
    public ResponseEntity<?> chatroom(@RequestParam(required = false) String keyword) {
        try {
            List<ChatRoomResponseDTO> chatRoomResponseDTO = chatRoomService.roomList(keyword);
            return new ResponseEntity<>(chatRoomResponseDTO, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("채팅방 목록을 가져오는데 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
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
