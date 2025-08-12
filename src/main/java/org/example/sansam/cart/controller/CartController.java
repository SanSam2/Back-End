package org.example.sansam.cart.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.example.sansam.cart.dto.*;
import org.example.sansam.cart.service.CartService;
import org.example.sansam.product.dto.TextResponse;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/carts")
public class CartController {
    private final CartService cartService;
    //장바구니 추가(상품 및 수량 추가), 장바구니 삭제, 장바구니 조회
    @Operation(summary = "장바구니 추가", description = "장바구니에 상품을 추가합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AddCartRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                        "userId": 1,
                                        "addCartItems": [
                                            {
                                                "productId": 6,
                                                "color":"RED",
                                                "size":"s",
                                                "quantity":5
                                            }
                                        ]
                                    }
                            """)
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "장바구니 생성 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TextResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                        "message": "장바구니 추가 완료"
                                    }
                                """))),
            @ApiResponse(responseCode = "400", description = "유효성 오류"),
            @ApiResponse(responseCode = "404", description = "사용자 또는 상품 없음")
    })
    @PostMapping("/add")
    public ResponseEntity<?> addCart(@RequestBody AddCartRequest addCartRequest){
        try{
            cartService.AddCartItem(addCartRequest);
            return ResponseEntity.ok(new TextResponse("장바구니 추가 완료"));
        }catch(IllegalArgumentException e){
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @Operation(summary = "장바구니 상품 삭제", description = "장바구니에서 상품을 삭제합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DeleteCartRequest.class),
                            examples = @ExampleObject(value = """
                                  {
                                        "userId": 1,
                                        "deleteCartItems": [
                                        {
                                          "productId": 1,
                                          "color": "BLACK",
                                          "size": "M"
                                        }
                                      ]
                                  }
                    """)
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "장바구니 상품 삭제 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                        "message": "장바구니 삭제 완료"
                                    }
                                """))),
            @ApiResponse(responseCode = "404", description = "장바구니 상품 없음")
    })
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteCart(@RequestBody DeleteCartRequest deleteCartRequest){
        try{
            cartService.deleteCartItem(deleteCartRequest);
            return ResponseEntity.ok(new TextResponse("장바구니 삭제 완료"));
        }catch(IllegalArgumentException e){
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @Operation(summary = "장바구니 조회", description = "장바구니에 담은 상품을 조회합니다.",
            parameters = {
                    @Parameter(name = "userId", in = ParameterIn.QUERY, required = true,
                            description = "유저 ID", example = "1"),
                    @Parameter(name = "page", in = ParameterIn.QUERY, required = false,
                            description = "page num", example = "1"),
                    @Parameter(name = "size", in = ParameterIn.QUERY, required = false,
                            description = "page size", example = "1")
            }
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "장바구니 조회 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                        "content": [
                                            {
                                                "searchListResponse": {
                                                      "productId": 6,
                                                      "productName": "반팔 3",
                                                      "price": 300300,
                                                      "url": "https://sansam2-bucket.s3.ap-northeast-2.amazonaws.com/images/25fab023-3052-4634-8236-c92898265582.img2.jpg",
                                                      "wish": false
                                                },
                                                "size": "S",
                                                "color": "RED",
                                                "quantity": 7,
                                                "stock": 86
                                            }
                                        ],
                                        "pageable": {
                                            "pageNumber": 0,
                                            "pageSize": 10,
                                            "sort": {
                                                "empty": false,
                                                "sorted": true,
                                                "unsorted": false
                                            },
                                            "offset": 0,
                                            "paged": true,
                                            "unpaged": false
                                        },
                                        "last": true,
                                        "totalElements": 1,
                                        "totalPages": 1,
                                        "first": true,
                                        "size": 10,
                                        "number": 0,
                                        "sort": {
                                            "empty": false,
                                            "sorted": true,
                                            "unsorted": false
                                        },
                                        "numberOfElements": 1,
                                        "empty": false
                                    }
                     """)))
    })
    @GetMapping("/search")
    public ResponseEntity<?> searchCart(
            @RequestParam Long userId,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size)
    {
        try {
            Page<SearchCartResponse> carts = cartService.searchCarts(userId, page, size);
            return ResponseEntity.ok(carts);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }


    @Operation(summary = "장바구니 수량 변경", description = "장바구니 상품 수량을 변경합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UpdateCartRequest.class),
                            examples = @ExampleObject(value = """
                            {
                                   "userId": 1,
                                   "productId":2,
                                   "color":"White",
                                   "size": "L",
                                   "quantity":3
                            }
                    """)
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수량 변경 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UpdateCartResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                             "searchListResponse": null,
                                             "size": "S",
                                             "color": "RED",
                                             "quantity": 3,
                                             "stock": 86
                                         }
                                """))),
            @ApiResponse(responseCode = "404", description = "상품 없음"),
            @ApiResponse(responseCode = "400", description = "유효성 오류")
    })
    @PatchMapping("/update")
    public ResponseEntity<?> updateCart(@RequestBody UpdateCartRequest updateCartRequest){
        try {
            SearchCartResponse response = cartService.updateCart(updateCartRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }
}
