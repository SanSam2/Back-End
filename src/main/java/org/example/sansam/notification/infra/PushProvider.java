package org.example.sansam.notification.infra;

public interface PushProvider {
    void push(Long userId, Long nhId, String eventName, String payloadJson);
    void broadcast(Long nhId, String eventName, String payloadJson);
}
