package org.example.sansam.notification.controller;

import lombok.RequiredArgsConstructor;
import org.example.sansam.notification.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/notification")
@RequiredArgsConstructor
public class AdminNotificationController {

    private final NotificationService notificationService;

    @PostMapping("/broadcast")
    public ResponseEntity<String> broadcast(@RequestBody Map<String, String> request) {
        String title = request.get("title");
        String content = request.get("content");

        notificationService.saveBroadcastNotification(title, content);
        return ResponseEntity.ok().body("공지 완료!" +
                "\ntitle=" + title +
                "\ncontent=" + content);
    }
}
