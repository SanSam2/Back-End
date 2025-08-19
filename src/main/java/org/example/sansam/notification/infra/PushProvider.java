package org.example.sansam.notification.infra;

public interface PushProvider {
    void push(Long userId, String eventName, String payloadJson);
}
