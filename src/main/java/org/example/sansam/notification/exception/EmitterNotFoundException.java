package org.example.sansam.notification.exception;

public class EmitterNotFoundException extends RuntimeException {
    public EmitterNotFoundException(Long userId) {
        super(String.format("사용자(%d)의 SSE 연결을 찾을 수 없습니다.", userId));
    }
}
