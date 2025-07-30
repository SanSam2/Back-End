package org.example.sansam.payment.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.example.sansam.order.dto.OrderRequest;
import org.example.sansam.order.dto.OrderResponse;
import org.example.sansam.order.dto.PaymentCancelRequest;
import org.example.sansam.payment.dto.TossCallBackRequest;
import org.example.sansam.payment.service.PaymentService;
import org.example.sansam.user.domain.User;
import org.example.sansam.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/pay")
public class PaymentController {

    private final PaymentService paymentService;
    private final UserRepository userRepository;

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
    @PostMapping("/confirm")
    public ResponseEntity<?> confirmPayment(@RequestBody OrderRequest orderRequest, String userEmail){
        try{
            Optional<User> userOpt = userRepository.findByEmail(orderRequest.getUserEmail());
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("사용자 없음");
            }

            User user = userOpt.get();

            OrderResponse response=paymentService.confirmPayment(orderRequest, user);
            return ResponseEntity.ok("결제 완료");
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
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
