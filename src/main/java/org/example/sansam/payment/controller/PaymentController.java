package org.example.sansam.payment.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.sansam.payment.dto.PaymentCancelRequest;
import org.example.sansam.payment.dto.TossPaymentRequest;
import org.example.sansam.payment.service.PaymentCancelService;
import org.example.sansam.payment.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/pay")
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentCancelService paymentCancelService;

    @PostMapping("/confirm")
    public ResponseEntity<?> confirmPayment(@RequestBody TossPaymentRequest request) {
        try {
            paymentService.confirmPayment(request);
            return ResponseEntity.ok("결제 승인 완료");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @Operation(summary = "결제 취소", description = "사용자의 결제를 취소 처리합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "결제가 성공적으로 취소되었습니다.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PaymentCancelRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                          "orderId": "ORD123456",
                                          "cancelReason": "단순 변심",
                                          "items": [
                                            {
                                              "productId": 1001,
                                              "cancelQuantity": 2
                                            },
                                            {
                                              "productId": 1002,
                                              "cancelQuantity": 1
                                            }
                                          ]
                                        }
                                """))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping("/cancel") //결제 취소 로직
    public ResponseEntity<?> handlePaymentCancel(
            @RequestBody PaymentCancelRequest cancelRequest) {
        try {

            String result = paymentCancelService.wantToCancel(cancelRequest);

            return ResponseEntity.ok("결제가 성공적으로 취소되었습니다.");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

}
