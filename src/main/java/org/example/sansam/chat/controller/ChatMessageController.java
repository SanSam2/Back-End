package org.example.sansam.chat.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class ChatMessageController {

    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate messagingTemplate;


    // 채팅방 메세지 조회
    @Operation(summary = "채팅방 메시지 조회", description = "특정 채팅방의 메시지 목록을 페이징 처리하여 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "메시지 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ChatMessageResponseDTO.class),
                            examples = @ExampleObject(value = """
                {
                  "content": [
                    {
                      "id": 123,
                      "message": "안녕하세요",
                      "createdAt": "2025-08-11T14:00:00",
                      "userName": "홍길동",
                      "sender": 45,
                      "roomId": 1
                    }
                  ],
                  "pageable": {
                    "pageNumber": 0,
                    "pageSize": 20
                  },
                  "totalPages": 5,
                  "totalElements": 100,
                  "last": false,
                  "first": true,
                  "numberOfElements": 20
                }
            """)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "로그인 필요",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"로그인이 필요합니다.\"}")
                    )
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"에러 메시지 내용\"}")
                    )
            )
    })
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
            log.info("메시지 처리중 오류");
        }
    }
}
