package org.example.sansam.chat.controller;

import jakarta.servlet.http.HttpSession;
import lombok.Getter;
import org.example.sansam.chat.dto.ChatRoomRequestDTO;
import org.example.sansam.chat.dto.ChatRoomResponseDTO;
import org.example.sansam.chat.service.ChatRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chatroom")
public class ChatRoomController {

    @Autowired
    private ChatRoomService chatRoomService;


    // 입장한 채팅방 목록
    @GetMapping("/user")
    public ResponseEntity<?> UserChatRoomGet(HttpSession session){

        try {
//            List<ChatRoomResponseDTO> chatRoomResponseDTO = chatRoomService.userRoomList(session);
//            return new ResponseEntity<>(chatRoomResponseDTO, HttpStatus.OK);
            return new ResponseEntity<>(HttpStatus.OK);
        }catch (Exception e){
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    // 채팅방 전체조회 , 검색
    @GetMapping
    public ResponseEntity<?> chatroom(@RequestParam(required = false) String keyword) {
        try {
//            List<ChatRoomResponseDTO> chatRoomResponseDTO = chatRoomService.roomList(keyword);
//            return new ResponseEntity<>(chatRoomResponseDTO, HttpStatus.OK);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("채팅방 목록을 가져오는데 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 채팅방 생성
    @PostMapping
    public ResponseEntity<?> createRoom(@RequestBody ChatRoomRequestDTO chatRoomRequestDTO, HttpSession session){
        try {
//            ChatRoomResponseDTO chatRoomResponseDTO = chatRoomService.createRoom(chatRoomRequestDTO, session);
//            return  new ResponseEntity<>(chatRoomResponseDTO, HttpStatus.OK);
            return  new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e){
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    // 채팅방입장
    @PostMapping("/{roomId}/enter")
    public ResponseEntity<?> chatroomEnter(@PathVariable String roomId,
                                           HttpSession session) {
        try{
//            ChatRoomResponseDTO chatRoomResponseDTO = chatRoomService.roomConnection(roomId, session);
//            return new ResponseEntity<>(chatRoomResponseDTO, HttpStatus.OK);
            return new ResponseEntity<>(HttpStatus.OK);

        }catch (Exception e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 채팅방 퇴장
    @DeleteMapping("/{roomId}/leave")
    public ResponseEntity<?> chatroomLeave(@PathVariable String roomId, HttpSession session) {
        try{
//            ChatRoomResponseDTO chatRoomResponseDTO = chatRoomService.roomLeave(roomId, session);
//            return  new ResponseEntity<>(chatRoomResponseDTO, HttpStatus.OK);
            return new ResponseEntity<>(HttpStatus.OK);
        }catch (Exception e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
