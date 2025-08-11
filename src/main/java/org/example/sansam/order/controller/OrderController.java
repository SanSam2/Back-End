package org.example.sansam.order.controller;


import lombok.RequiredArgsConstructor;
import org.example.sansam.order.dto.OrderRequest;
import org.example.sansam.order.dto.OrderResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {


    @PostMapping("/create") //주문서 작성
    public ResponseEntity<OrderResponse> createOrder(@RequestBody OrderRequest orderRequest){
        OrderResponse response = new OrderResponse();
        //order Response는, 주문 넣어서 결제에 필요한 주문서에 들어있는 정보를 긁어오게 됩니다.

        try{
            return ResponseEntity.ok(response);

        }catch (Exception e){
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/{orderId}") //주문 정보 단건 조회 (하나만 조회 = 주문 상세 조회)
    public ResponseEntity<?> getOrderById(){
        OrderResponse response = new OrderResponse();
        try{

            return ResponseEntity.status(HttpStatus.OK).body(response);
        }catch(Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("주문하신 상품이 없습니다.");
        }
    }

    @GetMapping("/getAllOrders") //주문 전체 조회
    public ResponseEntity<?> getAllOrders(){
        try{
            return ResponseEntity.ok("대충 전체 조회한 주문 리스트 반환");
        }catch(Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("전체 주문 조회 실패");
        }
    }

    @DeleteMapping("/deleteOrder")
    public ResponseEntity<?> deleteOrders(){
        try{
            return ResponseEntity.ok("delete성공");
        }catch(Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }







}
