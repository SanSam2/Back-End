package org.example.sansam.notification.controller;

import lombok.RequiredArgsConstructor;
import org.example.sansam.notification.event.sse.BroadcastEvent;
import org.example.sansam.notification.infra.PushProvider;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/test/notifications")
@RequiredArgsConstructor
public class BroadcastTestController {

    private final PushProvider pushProvider;

    @PostMapping("/broadcast")
    public ResponseEntity<String> broadcast(@RequestBody Map<String, String> request) {
        String title = request.getOrDefault("title", "테스트 공지");
        String content = request.getOrDefault("content", "내용 없음");

        long timestamp = System.currentTimeMillis();

        String payload = String.format(
                "{\"title\":\"%s\", \"message\":\"%s\", \"timestamp\":%d}",
                title, content, timestamp
        );

        pushProvider.broadcast("broadcast", payload);

        return ResponseEntity.ok("테스트 broadcast 발송 완료!");
    }
}
