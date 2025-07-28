package org.example.sansam.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.example.sansam.notification.service.EmailService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notify-email")
public class EmailController {

    private final EmailService emailService;

    @Operation(summary = "테스트 API", description = "정상/예외 응답 확인",
            responses = {
                    @ApiResponse(responseCode = "200", description = "정상 응답"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "500", description = "서버 에러")
            })

    @PostMapping("/payment-complete")
    public ResponseEntity<?> createPaymentCompleteEmail() {
        try {

            return ResponseEntity.ok().body("결제 완료 이메일 발송 성공");
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("결제 완료 이메일 발송 실패");
        }
    }

    @PostMapping("/payment-cancel")
    public ResponseEntity<?> createPaymentCancelEmail() {
        try {

            return ResponseEntity.ok().body("결제 취소 이메일 발송 성공");
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("결제 취소 이메일 발송 실패");
        }
    }

    @PostMapping("/welcome")
    public ResponseEntity<?> createWelcomeEmail() {
        try {

            return ResponseEntity.ok().body("회원 가입 환영 이메일 발송 성공");
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("회원 가입 환영 이메일 발송 실패");
        }
    }
}
