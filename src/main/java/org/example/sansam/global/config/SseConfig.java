//package org.example.sansam.global.config;
//
//import org.example.sansam.notification.infra.PushProvider;
//import org.example.sansam.notification.service.PushProvider;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
//
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//
//@Configuration
//public class SseConfig {
////    private final Map<Long, SseEmitter> sseEmitters = new ConcurrentHashMap<>();
//
//    @Bean
//    public SseEmitter sseEmitter() {
//        SseEmitter existingEmitter = sseEmitters.get(userId);
//        if (existingEmitter != null) {
//            existingEmitter.complete(); // 이전 연결 정리
//            sseEmitters.remove(userId);
//        }
//
//        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
//        sseEmitters.put(userId, emitter);
//
//        // 연결 종료 시 emitter 제거
//        emitter.onCompletion(() -> sseEmitters.remove(userId));
//        emitter.onTimeout(() -> sseEmitters.remove(userId));
//        emitter.onError(ex -> sseEmitters.remove(userId));
//
//        return emitter;
//    }
//}
