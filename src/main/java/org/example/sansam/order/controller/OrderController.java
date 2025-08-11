package org.example.sansam.order.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.sansam.exception.pay.ErrorCode;
import org.example.sansam.order.dto.OrderItemDto;
import org.example.sansam.order.dto.OrderResponse;
import org.example.sansam.order.dto.OrderWithProductsResponse;
import org.example.sansam.order.service.OrderProductService;
import org.example.sansam.order.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {

    private final OrderProductService orderProductService;
    private final OrderService orderService;

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

    @GetMapping("/{orderId}") //주문 정보 단건 상세 조회 (하나만 조회 = 주문 1건에 대한 조회)
    public ResponseEntity<?> getOrderByOrderId(@PathVariable Long orderId) {
        try{
            List<OrderItemDto> items = orderProductService.findByOrderId(orderId);

            if(items.isEmpty()){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorCode.PRODUCT_NOT_FOUND.getMessage());
            }
            return ResponseEntity.status(HttpStatus.OK).body(items);
        }catch(Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErrorCode.INTERNAL_SERVER_ERROR.getMessage());
        }
    }


    @Operation(summary = "회원 주문 목록 조회", description = "회원 ID를 기준으로 주문 전체 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "주문 목록 조회 성공",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = OrderResponse.class)),
                            examples = @ExampleObject(value = """
                            [
                              {
                                "orderId": 1,
                                "userId": 2,
                                {
                                    orderItemName : 상품명
                                    Itemprice : 상품 가격
                                    ItemSize : 상품 사이즈
                                    ItemColor : 상품 색상 
                                    ItemQuantity : 상품 구매 개수
                                }
                                {
                                    orderItemName : 상품명
                                    Itemprice : 가격
                                    ItemSize : 사이즈
                                    ItemColor : 색상
                                    ItemQuantity : 개수
                                }
                                "totalAmount": 50000,
                                "orderStatus": "ORDERED",
                                "createdAt": "2025-07-31T13:30:00"
                              },
                              {
                                "orderId": 2,
                                "userId": 2,
                                "totalAmount": 80000,
                                "orderStatus": "DELIVERED",
                                "createdAt": "2025-07-31T14:00:00"
                              }
                            ]
                        """))),
            @ApiResponse(responseCode = "400", description = "회원 정보 없음",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "회원의 주문 내역이 없습니다.")))
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getOrdersByUserId(@PathVariable Long userId, @RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "5") int size) {
        try {
            Page<OrderWithProductsResponse> orders = orderService.getAllOrdersByUserId(userId,page, size);
            if (orders.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorCode.ORDER_NOT_FOUND.getMessage());
            }
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErrorCode.INTERNAL_SERVER_ERROR.getMessage());
        }
    }


}
