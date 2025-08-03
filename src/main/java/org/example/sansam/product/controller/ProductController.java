package org.example.sansam.product.controller;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.sansam.product.domain.ProductDetail;
import org.example.sansam.product.dto.*;
import org.example.sansam.product.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    // 상품 품절 처리
    @PatchMapping ("/soldout")
    public ResponseEntity<?> markSoldOut(@RequestBody SoldoutRequest soldoutRequest) {
        try {
            SoldoutResponse response = new SoldoutResponse();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    //상품 재고 조회 - 품절 시 반환값 ㅒ
    @GetMapping("/search-stock")
    public ResponseEntity<?> searchStock(@RequestBody SearchStockRequest searchStockRequest) {
        try {
            SearchStockResponse response = new SearchStockResponse();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }


    //상품 재고 차감
    @PatchMapping("/decrease-stock")
    public ResponseEntity<?> decreaseStock(@RequestBody DecreaseStockRequest decreaseStockRequest) {
        try {
            SearchStockRequest response = new SearchStockRequest();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    //상품 상세 조회
    @PostMapping("/{productId}")
    public ResponseEntity<ProductResponse> getDefaultOption(@PathVariable Long productId, @RequestBody SearchRequest searchRequest) {
        return ResponseEntity.ok(productService.getProduct(productId, searchRequest));
    }

//    @GetMapping("/{productId}/option")
//    public ResponseEntity<ProductDetailResponse> getColorOption(
//            @PathVariable Long productId,
//            @RequestParam String color
//    ) {
//        return ResponseEntity.ok(productService.getProductByColor(productId, color));
//    }

}
