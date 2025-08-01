package org.example.sansam.notification.exception;

public class NotificationDeliveryException extends RuntimeException {
  public NotificationDeliveryException(Long userId, String reason) {
    super(String.format("사용자(%d)에게 알림 전송 실패: %s", userId, reason));
  }
}
