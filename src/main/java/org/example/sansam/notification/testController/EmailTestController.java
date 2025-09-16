package org.example.sansam.notification.testController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.example.sansam.notification.service.EmailService;
import org.example.sansam.user.domain.User;
import org.example.sansam.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notify-email")
@Tag(name = "Email", description = "이메일 발송 API")
@Builder
public class EmailTestController {

    private final EmailService emailService;
    private final UserRepository userRepository;

    @Operation(summary = "테스트 API", description = "정상/예외 응답 확인",
            responses = {
                    @ApiResponse(responseCode = "200", description = "정상 응답"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "500", description = "서버 에러")
            })

    @PostMapping("/payment-complete")
    public ResponseEntity<String> createPaymentCompleteEmail() {
        try {
            Optional<User> user = userRepository.findById(2L);
            User tmpUser = user.orElseThrow();

            emailService.sendPaymentCompletedEmail(tmpUser, "산삼반팔 외 2건", 28000L);
            return ResponseEntity.ok().body("결제 완료 이메일 발송 성공");
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("결제 완료 이메일 발송 실패");
        }
    }

    @PostMapping("/payment-cancel")
    public ResponseEntity<String> createPaymentCancelEmail() {
        try {
            Optional<User> user = userRepository.findById(2L);
            User tmpUser = user.orElseThrow();

            emailService.sendPaymentCanceledMessage(tmpUser, "산삼반팔 외 2건", 28000L);
            return ResponseEntity.ok().body("결제 취소 이메일 발송 성공");
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("결제 취소 이메일 발송 실패");
        }
    }

    @PostMapping("/welcome")
    public ResponseEntity<String> createWelcomeEmail() {
        try {
            Optional<User> user = userRepository.findById(2L);
            User tmpUser = user.orElseThrow();

            emailService.sendWelcomeEmail(tmpUser);
            return ResponseEntity.ok().body("회원 가입 환영 이메일 발송 성공");
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("회원 가입 환영 이메일 발송 실패");
        }
    }
}
