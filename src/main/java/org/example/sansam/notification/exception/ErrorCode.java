package org.example.sansam.notification.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum ErrorCode {
    NOTIFICATION_TEMPLATE_NOT_FOUND(HttpStatus.NOT_FOUND, "Notification template not found"),
    NOTIFICATION_DELIVERY_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Notification delivery failed"),
    EMITTER_NOT_FOUND(HttpStatus.NOT_FOUND, "Emitter not found");

    private final HttpStatus status;
    private final String message;
}
