package org.example.sansam.chat.controller;

import jakarta.servlet.http.HttpSession;
import org.example.sansam.chat.dto.ChatMessageRequestDTO;
import org.example.sansam.chat.service.ChatMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chatroom")
public class ChatMessageController {

    @Autowired
    private ChatMessageService chatMessageService;


    @GetMapping("/{roomId}/message")
    public ResponseEntity<?> getRoomMessages(@PathVariable String roomId) {
        try{
//            List<ChatMessageResponseDTO> chatMessageResponseDTO = chatMessageService.getMessages(roomId);
//            return new ResponseEntity<>(chatMessageResponseDTO, HttpStatus.OK);

            return new ResponseEntity<>(HttpStatus.OK);
        }catch (Exception e){
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @PostMapping("/{roomId}/message")
    public ResponseEntity<?> addMessage(@PathVariable String roomId,
                                        @RequestBody ChatMessageRequestDTO chatMessageRequestDTO,
                                        HttpSession session) {
        try {
//            ChatRoomResponseDTO chatRoomResponseDTO = chatMessageService.addMessage(roomId, chatMessageRequestDTO, session);
//            return new ResponseEntity<>(chatRoomResponseDTO, HttpStatus.OK);
            return new ResponseEntity<>(HttpStatus.OK);


        }catch (Exception e){
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @DeleteMapping("/{messageId}/message")
    public ResponseEntity<?> deleteRoomMessage(@PathVariable String messageId, HttpSession session) {
        try{
//            chatMessageService.deleteMessage(messageId, session);
            return new ResponseEntity<>(HttpStatus.OK);
        }catch (Exception e){
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }
}
