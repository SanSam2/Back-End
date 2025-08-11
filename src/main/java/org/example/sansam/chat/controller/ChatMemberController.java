package org.example.sansam.chat.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.example.sansam.chat.dto.LastReadDTO;
import org.example.sansam.chat.service.ChatMemberService;
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
