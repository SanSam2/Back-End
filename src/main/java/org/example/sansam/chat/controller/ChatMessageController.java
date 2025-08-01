package org.example.sansam.chat.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.sansam.chat.dto.ChatMessageRequestDTO;
import org.example.sansam.chat.dto.ChatMessageResponseDTO;
import org.example.sansam.chat.service.ChatMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/chatroom")
public class ChatMessageController {

    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate messagingTemplate;


    // 채팅방 메세지 조회
    @GetMapping("/{roomId}/message")
    public ResponseEntity<?> getRoomMessages(@PathVariable Long roomId,
                                                   @RequestParam(required = false) Long lastMessageId,
                                                   @RequestParam(defaultValue = "20") int size) {
        try{
            List<ChatMessageResponseDTO> chatMessageResponseDTOS = chatMessageService.getMessages(roomId, lastMessageId, size);
            return new ResponseEntity<>(chatMessageResponseDTOS, HttpStatus.OK);
        }catch (Exception e){
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    // 메세지 전송
    @MessageMapping("/chat/{roomId}/message")
    public void handleMessage(@DestinationVariable Long roomId,
                              ChatMessageRequestDTO chatMessageRequestDTO,
                              HttpSession session) {
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                throw new IllegalStateException("User not authenticated");
            }

            ChatMessageResponseDTO chatMessageResponseDTO = chatMessageService.addMessage(chatMessageRequestDTO, userId, roomId);

            messagingTemplate.convertAndSend(
                    "/sub/chat/room/" + roomId, chatMessageResponseDTO
            );
        } catch (Exception e) {
            System.out.print("채팅 메시지 처리 중 예외 발생");
        }
    }

    // 본인 메세지 삭제
    @DeleteMapping("/{messageId}/message")
    public ResponseEntity<?> deleteRoomMessage(@PathVariable Long messageId, HttpSession session) {
        try{
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                throw new IllegalStateException("User not authenticated");
            }

            chatMessageService.deleteMessage(messageId, userId);
            return new ResponseEntity<>(HttpStatus.OK);
        }catch (Exception e){
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }
}
