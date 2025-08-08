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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderRestController {

    private final OrderService orderService;


    @Operation(summary = "주문 저장", description = "새로운 주문을 저장합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "주문 저장 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = OrderResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                        "orderId": 1,
                                        "orderNumber": "20250801144325-2ef057a8",
                                        "orderName": "구찌 반팔티 외 1건",
                                        "totalAmount": 2450000,
                                        "status": "waiting",
                                        "createdAt": "2025-08-01T14:43:25.398035"
                                    }
                                """))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "상품 정보가 올바르지 않습니다.")))
    })
    @PostMapping("/save") //주문 요청
    public ResponseEntity<?> saveOrder(@RequestBody OrderRequest orderRequest){
        log.error("여기까지는 들어왔당");
        try{
            OrderResponse response = orderService.saveOrder(orderRequest);
            return ResponseEntity.ok(response);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
