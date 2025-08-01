package org.example.sansam.order.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.sansam.order.dto.OrderRequest;
import org.example.sansam.order.dto.OrderResponse;
import org.example.sansam.order.service.OrderService;
import org.example.sansam.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final UserRepository userRepository;
    private final OrderService orderService;


    @Operation(summary = "주문 저장", description = "새로운 주문을 저장합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "주문 저장 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = OrderResponse.class),
                            examples = @ExampleObject(value = """
                                {
                                  "orderId" : "1"
                                  "orderNumber": "20250731132640-3b80042b",
                                  "totalAmount": 50000,
                                  "orderStatus": "waiting",
                                  "createdAt": "2025-07-31T13:30:00"
                                }
                                """))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "상품 정보가 올바르지 않습니다.")))
    })
    @PostMapping("/save") //주문 요청
    public ResponseEntity<?> saveOrder(@RequestBody OrderRequest orderRequest){

        try{
            OrderResponse response = orderService.saveOrder(orderRequest);
            return ResponseEntity.ok(response);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }



    @Operation(summary = "주문 상세 조회", description = "주문 ID를 기준으로 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "주문 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = OrderResponse.class),
                            examples = @ExampleObject(value = """
                                {
                                  "orderId": 1,
                                  "userId": 2,
                                  "totalAmount": 50000,
                                  "orderStatus": "ORDERED",
                                  "createdAt": "2025-07-31T13:30:00"
                                }
                                """))),
            @ApiResponse(responseCode = "400", description = "주문 정보 없음",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "주문하신 상품이 없습니다.")))
    })
    @GetMapping("/{orderId}") //주문 정보 단건 조회 (하나만 조회 = 주문 상세 조회)
    public ResponseEntity<?> getOrderById(){
        OrderRequest request= new OrderRequest(); //에러방지용 코드

        OrderResponse response = orderService.saveOrder(request);//에러방지용 코드
        try{

            return ResponseEntity.status(HttpStatus.OK).body(response);
        }catch(Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("주문하신 상품이 없습니다.");
        }
    }


    @Operation(summary = "전체 주문 조회", description = "등록된 전체 주문 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "전체 주문 조회 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                [
                                  {
                                    "orderId": 1,
                                    "userId": 2,
                                    "totalAmount": 50000,
                                    "orderStatus": "ORDERED",
                                    "createdAt": "2025-07-31T13:30:00"
                                  },
                                  {
                                    "orderId": 2,
                                    "userId": 3,
                                    "totalAmount": 80000,
                                    "orderStatus": "DELIVERED",
                                    "createdAt": "2025-07-31T14:00:00"
                                  }
                                ]
                                """))),
            @ApiResponse(responseCode = "500", description = "조회 실패",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "전체 주문 조회 실패")))
    })
    @GetMapping("/getAllOrders") //주문 전체 조회
    public ResponseEntity<?> getAllOrders(){
        try{
            return ResponseEntity.ok("대충 전체 조회한 주문 리스트 반환");
        }catch(Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("전체 주문 조회 실패");
        }
    }

    @Operation(summary = "주문 삭제", description = "주문 내역을 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "delete성공"))),
            @ApiResponse(responseCode = "500", description = "삭제 실패",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "서버 오류로 삭제 실패")))
    })
    @DeleteMapping("/deleteOrder")
    public ResponseEntity<?> deleteOrders(){
        try{
            return ResponseEntity.ok("delete성공");
        }catch(Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }







}
