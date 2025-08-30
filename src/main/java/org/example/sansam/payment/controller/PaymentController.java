package org.example.sansam.payment.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.sansam.payment.dto.CancelResponse;
import org.example.sansam.payment.dto.PaymentCancelRequest;
import org.example.sansam.payment.dto.TossPaymentRequest;
import org.example.sansam.payment.dto.TossPaymentResponse;
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
    public ResponseEntity<?> confirmPayment(@RequestBody TossPaymentRequest request){
        TossPaymentResponse response = paymentService.confirmPayment(request);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/cancel") //결제 취소 로직
    public ResponseEntity<?> handlePaymentCancel(
            @RequestBody PaymentCancelRequest cancelRequest) {
        CancelResponse result = paymentCancelService.wantToCancel(cancelRequest);
        return ResponseEntity.ok(result);
    }

}
