package org.example.sansam.notification.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class ErrorResponse {
    private final int status;
    private final String code;
    private final String message;
    private final LocalDateTime timestamp;

    public static ErrorResponse of(ErrorCode ec) {
        return new ErrorResponse(
                ec.getStatus().value(),
                ec.name(),
                ec.getMessage(),
                LocalDateTime.now()
        );
    }

    public static ErrorResponse of(HttpStatus status, String code, String message) {
        return new ErrorResponse(status.value(), code, message, LocalDateTime.now());
    }
}
