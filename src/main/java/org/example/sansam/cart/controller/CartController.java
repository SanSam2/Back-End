package org.example.sansam.cart.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.example.sansam.cart.dto.*;
import org.example.sansam.cart.service.CartService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/carts")
public class CartController {
    private final CartService cartService;
    //장바구니 추가(상품 및 수량 추가), 장바구니 삭제, 장바구니 조회
    @Operation(summary = "테스트 API", description = "정상/예외 응답 확인",
            responses = {
                    @ApiResponse(responseCode = "200", description = "정상 응답"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "500", description = "서버 에러")
            })
    @PostMapping("/add")
    public ResponseEntity<?> addCart(@RequestBody AddCartRequest addCartRequest){
        try{
            cartService.AddCartItem(addCartRequest);
            return ResponseEntity.ok("장바구니 추가 완료");
        }catch(IllegalArgumentException e){
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteCart(@RequestBody DeleteCartRequest deleteCartRequest){
        try{
            cartService.deleteCartItem(deleteCartRequest);
            return ResponseEntity.ok("장바구니 삭제 완료");
        }catch(IllegalArgumentException e){
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

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
