package org.example.sansam.notification.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.sansam.notification.dto.BroadcastDTO;
import org.example.sansam.notification.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin/notifications")
@RequiredArgsConstructor
@Log4j2
public class AdminNotificationController {

    private final NotificationService notificationService;

    @PostMapping("/broadcast")
    public ResponseEntity<String> broadcast(@RequestBody BroadcastDTO request) {
        log.info("📥 Broadcast 요청 들어옴 - title={}, content={}", request.getTitle(), request.getContent());
        notificationService.saveBroadcastNotification(request.getTitle(), request.getContent(), LocalDateTime.now());
        return ResponseEntity.ok().body("공지 완료!" +
                "\ntitle=" + request.getTitle() +
                "\ncontent=" + request.getContent());
    }
}
