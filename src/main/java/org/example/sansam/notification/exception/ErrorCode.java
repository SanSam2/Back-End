package org.example.sansam.notification.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum ErrorCode {
    NOTIFICATION_DELIVERY_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Notification delivery failed"),
    NOTIFICATION_TEMPLATE_NOT_FOUND(HttpStatus.NOT_FOUND, "Notification template not found"),
    NOTIFICATION_HISTORY_ID_NOT_FOUND(HttpStatus.NOT_FOUND, "Notification histories id not found"),
    NOTIFICATION_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Notification save failed"),
    NOTIFICATION_SERIALIZATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Notification serialization failed"),
    NOTIFICATION_DESERIALIZATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Notification deserialization failed"),
    NOTIFICATION_TEMPLATE_FORMAT_ERROR(HttpStatus.BAD_REQUEST, "Notification template format error"),
    NOTIFICATION_BROADCAST_NOT_FOUND(HttpStatus.NOT_FOUND, "Notification broadcast not found"),
    EMITTER_NOT_FOUND(HttpStatus.NOT_FOUND, "Emitter not found");

    private final HttpStatus status;
    private final String message;
}
