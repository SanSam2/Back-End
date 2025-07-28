package org.example.sansam.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.example.sansam.notification.dto.NotificationRequestDTO;
import org.example.sansam.notification.service.NotificationService;
import org.example.sansam.notification.service.SseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final SseService sseService;

    @Operation(summary = "테스트 API", description = "정상/예외 응답 확인",
            responses = {
                    @ApiResponse(responseCode = "200", description = "정상 응답"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "500", description = "서버 에러")
            })

    @GetMapping("/subscribe")
    public SseEmitter subscribe(@RequestBody NotificationRequestDTO notificationRequestDTO) throws Exception {
        SseEmitter sseEmitter = sseService.connect(Integer.valueOf(notificationRequestDTO.getUserId()));
        try {
            return sseService.connect(Integer.valueOf(notificationRequestDTO.getUserId()));
        }catch (Exception e) {
            return sseEmitter;
        }
    }

    @PostMapping("/welcome")
    public ResponseEntity<?> createWelcomeNotification(@RequestBody NotificationRequestDTO notificationRequestDTO) {
        try {
            return ResponseEntity.ok().body("회원 가입 축하 메시지 발송 성공");
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("회원 가입 축하 메시지 발송 실패");
        }
    }

    @PostMapping("/payment-complete")
    public ResponseEntity<?> createPaymentCompleteNotification(@RequestBody NotificationRequestDTO notificationRequestDTO) {
        try {
            return ResponseEntity.ok().body("결제 완료 내역 메시지 발송 성공");
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("결제 완료 내역 메시지 발송 실패");
        }
    }

    @PostMapping("/payment-cancel")
    public ResponseEntity<?> createPaymentCancelNotification(@RequestBody NotificationRequestDTO notificationRequestDTO) {
        try {
            return ResponseEntity.ok().body("결제 취소 내역 메시지 발송 성공");
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("결제 완료 내역 메시지 발송 성공");
        }
    }

    @PostMapping("/review-request")
    public ResponseEntity<?> createPaymentReviewNotification(@RequestBody NotificationRequestDTO notificationRequestDTO) {
        try {
            return ResponseEntity.ok().body("리뷰 요청 알림 메시지 발송 성공");
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("리뷰 요청 알림 메시지 발송 실패");
        }
    }

    @PostMapping("/cart-low")
    public ResponseEntity<?> createStockLowNotification(@RequestBody NotificationRequestDTO notificationRequestDTO) {
        try {
            return ResponseEntity.ok().body("장바구니 재고 품절 임박 알림 메시지 발송 성공");
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("장바구니 재고 품절 임박 알림 메시지 발송 실패");
        }
    }

    @PostMapping("/wishList-low")
    public ResponseEntity<?> createWishListLowNotification(@RequestBody NotificationRequestDTO notificationRequestDTO) {
        try {
            return ResponseEntity.ok().body("위시리스트 재고 품절 임박 알림 메시지 발송 성공");
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("위시리스트 재고 품절 임박 알림 메시지 발송 실패");
        }
    }

    @PostMapping("/chat")
    public ResponseEntity<?> createChatNotification(@RequestBody NotificationRequestDTO notificationRequestDTO) {
        try {
            return ResponseEntity.ok().body("채팅 알림 메시지 발송 성공");
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("채팅 알림 메시지 발송 실패");
        }
    }

}
