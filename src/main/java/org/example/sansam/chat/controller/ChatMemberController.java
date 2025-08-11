package org.example.sansam.chat.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.example.sansam.chat.dto.LastReadDTO;
import org.example.sansam.chat.service.ChatMemberService;
import org.example.sansam.order.dto.OrderResponse;
import org.example.sansam.user.dto.LoginResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/chatroom")
public class ChatMemberController {

    private final ChatMemberService chatMemberService;

    @MessageMapping("/chat/last-read")
    public void handleLastRead(@Payload LastReadDTO dto, SimpMessageHeaderAccessor headerAccessor) {

        LoginResponse loginUser = (LoginResponse) headerAccessor.getSessionAttributes().get("loginUser");
        if (loginUser == null) {
            throw new IllegalStateException("User not authenticated");
        }
        Long userId = loginUser.getId();
        chatMemberService.updateLastReadAt(userId,
                dto.getRoomId(),
                LocalDateTime.now());
    }

    // 새로고침시 읽은날짜 로드
    @Operation(summary = "읽은 날짜 수정", description = "해당 방의 마지막 읽은 날짜를 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "날짜 수정 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{}")  // 응답 본문 없음 표시
                    )),
            @ApiResponse(responseCode = "401", description = "로그인 필요",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"마지막읽은 날짜를 수정하려면 로그인이 필요합니다.\"}")))
    })
    @PutMapping("/last_read")
    public ResponseEntity<Void> lastRead(
            @RequestBody LastReadDTO dto,
            HttpSession session) {

        LoginResponse loginUser = (LoginResponse) session.getAttribute("loginUser");
        if (loginUser == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Long userId = loginUser.getId();
        chatMemberService.updateLastReadAt(
                userId,
                dto.getRoomId(),
                LocalDateTime.now()
        );
        return ResponseEntity.ok().build();
    }
}
