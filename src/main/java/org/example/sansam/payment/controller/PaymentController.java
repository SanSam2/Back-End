package org.example.sansam.payment.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.sansam.order.dto.PaymentCancelRequest;
import org.example.sansam.payment.dto.TossCallBackRequest;
import org.example.sansam.payment.dto.TossPaymentRequest;
import org.example.sansam.payment.dto.TossPaymentResponse;
import org.example.sansam.payment.service.PaymentService;
import org.example.sansam.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/pay")
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;
    private final UserService userService;



    @Operation(summary = "토스 결제 콜백 처리", description = "토스 서버로부터 받은 결제 성공/실패 응답을 처리합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "결제 성공"),
            @ApiResponse(responseCode = "400", description = "결제 실패"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping("/callback") //토스 API로 부터 받은 응답 처리
    public ResponseEntity<?> handlePaymentCallback(@RequestBody TossCallBackRequest callBackRequest){
        try{
            if(callBackRequest!=null){ //결제가 성공한 경우


                return ResponseEntity.ok("결제 성공");
            }else{
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("결제 실패");
            }

        }catch(Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 내부 오류");
        }
    }



    @Operation(summary = "주문서 컨펌", description = "새로운 주문서를 승인하고, 결제로 넘어갈 수 있도록 합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "주문 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @GetMapping("/requestPayment")
    public ResponseEntity<?> requestPayment(@RequestBody TossPaymentRequest paymentRequest, Long userId){
        try{
            TossPaymentResponse response=paymentService.requestPayment(paymentRequest, userId);
            return ResponseEntity.ok("결제 완료");
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/confirmPayment")
    public ResponseEntity<?> confirmPayment(@RequestBody TossCallBackRequest request){
        TossPaymentResponse result = new TossPaymentResponse();
        result.setPaymentKey(request.getPaymentKey());
        result.setOrderId(String.valueOf(request.getOrderId())); //이거 맞는지 검토해봐야겠는디
        result.setAmount(request.getAmount());

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("message", "Success 결제 요청에 성공하였습니다.");
        resultMap.put("data", result);

        return ResponseEntity.ok(resultMap);
    }


    @Operation(summary = "결제 취소", description = "사용자의 결제를 취소 처리합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "결제 취소 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping("/cancel") //결제 취소 로직
    public ResponseEntity<?> handlePaymentCancel(@RequestBody PaymentCancelRequest cancelRequest){

        try{

            //결제 취소 진행
            return ResponseEntity.ok("결제 취소되었습니다.");

        }catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }
    }
}
