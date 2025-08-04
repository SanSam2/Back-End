package org.example.sansam.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.sansam.notification.service.NotificationService;
import org.example.sansam.notification.service.NotificationTestService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.yaml.snakeyaml.emitter.EmitterException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
@Log4j2
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "테스트 API", description = "정상/예외 응답 확인",
            responses = {@ApiResponse(responseCode = "200", description = "정상 응답"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "500", description = "서버 에러")})

    @GetMapping(value = "/subscribe", produces = "text/event-stream; charset=UTF-8")
    public ResponseEntity<SseEmitter> subscribe(@RequestParam Long userId) {
        try {
            log.info("SSE 구독 요청 - userId: {}", userId);
            SseEmitter emitter = notificationService.connect(userId);
            return ResponseEntity.ok(emitter);
        } catch (EmitterException e) {
            // 커스텀 예외로 명확하게 알 수 있게
            log.error("SSE 연결 실패 - userId: {}", userId, e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        } catch (Exception e) {
            log.error("알 수 없는 SSE 연결 오류 - userId: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }







    private final NotificationTestService notificationTestService;


    @PostMapping("/welcome")
    public ResponseEntity<Void> sendTestNotification(@RequestParam Long userId,
                                                     @RequestParam String username) {
        notificationTestService.sendWelcomeTestNotification(userId, username);
        return ResponseEntity.ok().build();
    }
    @PostMapping("/payment-complete")
    public ResponseEntity<Void> sendTest1Notification(@RequestParam Long userId,
                                                     @RequestParam String orderName, @RequestParam Long orderPrice) {
        notificationTestService.sendPaymentCompleteTestNotification(userId, orderName, orderPrice);
        return ResponseEntity.ok().build();
    }
    @PostMapping("/payment-cancel")
    public ResponseEntity<Void> sendTest2Notification(@RequestParam Long userId,
                                                      @RequestParam String orderName, @RequestParam Long refundPrice) {
        notificationTestService.sendPaymentCancelTestNotification(userId, orderName, refundPrice);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/cart-low")
    public ResponseEntity<Void> sendTest3Notification(@RequestParam Long userId,
                                                      @RequestParam String productName) {
        notificationTestService.sendCartLowTestNotification(userId, productName);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/wishlist-low")
    public ResponseEntity<Void> sendTest4Notification(@RequestParam Long userId,
                                                      @RequestParam String productName) {
        notificationTestService.sendWishListLowTestNotification(userId, productName);
        return ResponseEntity.ok().build();
    }

//    @PostMapping("/review-request")
//    public ResponseEntity<Void> sendTest5Notification(@RequestParam Long userId,
//                                                      @RequestParam String productName) {
//        notificationTestService.sendReviewRequestTestNotification(userId, productName);
//        return ResponseEntity.ok().build();
//    }

    @PostMapping("/chat")
    public ResponseEntity<Void> sendTest6Notification(@RequestParam Long userId,@RequestParam Long senderId,
                                                      @RequestParam String senderName) {
        notificationTestService.sendChatTestNotification(userId, senderId, senderName);
        return ResponseEntity.ok().build();
    }
}
