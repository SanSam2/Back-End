package org.example.sansam.chat.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.example.sansam.chat.dto.ChatMessageRequestDTO;
import org.example.sansam.chat.dto.ChatMessageResponseDTO;
import org.example.sansam.chat.dto.ChatMessageSendResponseDTO;
import org.example.sansam.chat.service.ChatMessageService;
import org.example.sansam.user.dto.LoginResponse;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/chatroom")
public class ChatMessageController {

    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate messagingTemplate;


    // 채팅방 메세지 조회
    @GetMapping("/{roomId}/message")
    public ResponseEntity<?> getRoomMessages(@PathVariable Long roomId,
                                             HttpSession session,
                                                   @RequestParam(required = false) Long lastMessageId,
                                                   @RequestParam(defaultValue = "20") int size) {
        try{
            LoginResponse loginUser = (LoginResponse) session.getAttribute("loginUser");
            if (loginUser == null) {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }

            Long userId = loginUser.getId();

            Page<ChatMessageResponseDTO> chatMessageResponseDTOS = chatMessageService.getMessages(roomId, lastMessageId, userId, size);
            return new ResponseEntity<>(chatMessageResponseDTOS, HttpStatus.OK);
        }catch (Exception e){
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    // 메세지 전송
    @MessageMapping("/chat/{roomId}/message")
    public void handleMessage(@DestinationVariable Long roomId,
                              ChatMessageRequestDTO chatMessageRequestDTO,
                              SimpMessageHeaderAccessor headerAccessor) {
        try {
            LoginResponse loginUser = (LoginResponse) headerAccessor.getSessionAttributes().get("loginUser");
            if (loginUser == null) {
                throw new IllegalStateException("User not authenticated");
            }
            Long userId = loginUser.getId();

            ChatMessageSendResponseDTO chatMessageSendResponseDTO = chatMessageService.addMessage(chatMessageRequestDTO, userId, roomId);

            messagingTemplate.convertAndSend(
                    "/sub/chat/room/" + roomId, chatMessageSendResponseDTO
            );
        } catch (Exception e) {
            System.out.print("채팅 메시지 처리 중 예외 발생");
        }
    }

    // 본인 메세지 삭제
    @MessageMapping("/chat/{roomId}/message/{messageId}/delete")
    public void deleteRoomMessage(@DestinationVariable Long roomId,
                                  @DestinationVariable Long messageId,
                                  SimpMessageHeaderAccessor headerAccessor) {
        try {
            LoginResponse loginUser = (LoginResponse) headerAccessor.getSessionAttributes().get("loginUser");
            if (loginUser == null) {
                throw new IllegalStateException("User not authenticated");
            }
            Long userId = loginUser.getId();

            chatMessageService.deleteMessage(messageId, userId, roomId);

            messagingTemplate.convertAndSend(
                    "/sub/chat/room/" + roomId + "/message.delete",
                    messageId
            );

        } catch (Exception e) {
            System.out.print("채팅 메시지 처리 중 예외 발생");

        }
    }
}
