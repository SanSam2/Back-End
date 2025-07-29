package org.example.sansam.chat.controller;

import jakarta.servlet.http.HttpSession;
import org.example.sansam.chat.dto.ChatRoomResponseDTO;
import org.example.sansam.chat.service.ChatRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chatroom")
public class ChatRoomController {

    @Autowired
    private ChatRoomService chatRoomService;

    @GetMapping
    public ResponseEntity<?> chatroom() {
        try{
//            List<ChatRoomResponseDTO> chatRoomResponseDTO = chatRoomService.roomList();
//            return new ResponseEntity<>(chatRoomResponseDTO, HttpStatus.OK);
            return new ResponseEntity<>(HttpStatus.OK);

        }catch (Exception e){
            return new ResponseEntity<>("채팅방 목록을 가져오는데 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

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
