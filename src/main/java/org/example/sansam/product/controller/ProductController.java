package org.example.sansam.product.controller;

import lombok.RequiredArgsConstructor;
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
    public ResponseEntity<?> markAsSoldOut(@RequestBody SoldoutRequest soldoutRequest) {
        try {
            SoldoutResponse response = new SoldoutResponse();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    //상품 재고 조회
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
}
