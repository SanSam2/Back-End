package org.example.sansam.notification.service;

import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SseProvider implements PushProvider{
    private final Map<Long, SseEmitter> sseEmitters = new ConcurrentHashMap<>();
    private final SseEmitter sseEmitter = new SseEmitter();

    @Override
    public void sendPushNotification(String title, String message) {

    }

    @Override
    public void sendEmailNotification(String title, String message) {

    }

    @Override
    public void connect() {

    }

    @Override
    public void disconnect() {

    }
}
