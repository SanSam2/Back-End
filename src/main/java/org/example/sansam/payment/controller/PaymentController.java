package org.example.sansam.payment.controller;


import org.example.sansam.order.dto.PaymentCancelRequest;
import org.example.sansam.payment.dto.TossCallBackRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pay")
public class PaymentController {


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
