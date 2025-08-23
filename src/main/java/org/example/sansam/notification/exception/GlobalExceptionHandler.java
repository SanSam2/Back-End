package org.example.sansam.notification.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.yaml.snakeyaml.emitter.EmitterException;

import java.time.LocalDateTime;

@RestControllerAdvice(basePackages = "org.example.sansam.notification.controller")
//@Order(Ordered.HIGHEST_PRECEDENCE) // 만약 다른 exceptionHandler가 있을 경우 우선 순위 둠
@Log4j2
public class GlobalExceptionHandler {

    // 1) 도메인 커스텀 예외
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustom(CustomException e) {
        log.warn("CustomException: {}", e.getMessage());
        return ResponseEntity.status(e.getErrorCode().getStatus())
                .body(ErrorResponse.of(e.getErrorCode()));
    }

    // 2) 유효성 검증 실패 (DTO @Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleInvalid(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(fe -> fe.getField() + " " + fe.getDefaultMessage())
                .orElse("Validation failed");
        ErrorResponse res = new ErrorResponse(400, "VALIDATION_ERROR", msg, LocalDateTime.now());
        return ResponseEntity.badRequest().body(res);
    }

    // 3) 쿼리/경로 파라미터 누락
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(MissingServletRequestParameterException e) {
        ErrorResponse res = new ErrorResponse(400, "MISSING_PARAMETER",
                e.getParameterName() + " is required", LocalDateTime.now());
        return ResponseEntity.badRequest().body(res);
    }

    // 4) 타입 변환 실패 (e.g. /{id}에 숫자 아닌 값)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        ErrorResponse res = new ErrorResponse(400, "TYPE_MISMATCH",
                "Invalid value for " + e.getName(), LocalDateTime.now());
        return ResponseEntity.badRequest().body(res);
    }

    // 5) JSON 파싱 실패
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(HttpMessageNotReadableException e) {
        ErrorResponse res = new ErrorResponse(400, "MESSAGE_NOT_READABLE",
                "Malformed JSON request", LocalDateTime.now());
        return ResponseEntity.badRequest().body(res);
    }

    // 6) 지원 안 하는 HTTP 메서드
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        ErrorResponse res = new ErrorResponse(405, "METHOD_NOT_ALLOWED",
                "Method not allowed", LocalDateTime.now());
        return ResponseEntity.status(405).body(res);
    }

    // 7) DB 제약조건 위반 등
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException e) {
        log.warn("DataIntegrityViolation: {}", e.getMessage());
        ErrorResponse res = new ErrorResponse(409, "DATA_INTEGRITY_VIOLATION",
                "Data integrity violation", LocalDateTime.now());
        return ResponseEntity.status(409).body(res);
    }

    // 8) 그 밖의 미처리 예외(최후의 보루)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnknown(Exception e, HttpServletRequest req) {
        log.error("Unexpected error on {} {}: {}", req.getMethod(), req.getRequestURI(), e.getMessage(), e);
        ErrorResponse res = new ErrorResponse(500, "INTERNAL_SERVER_ERROR",
                "Unexpected server error", LocalDateTime.now());
        return ResponseEntity.internalServerError().body(res);
    }
}
