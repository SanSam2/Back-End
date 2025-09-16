package org.example.sansam.product.controller;

import lombok.RequiredArgsConstructor;
import org.example.sansam.product.dto.ChangStockRequest;
import org.example.sansam.product.dto.ProductStatusResponse;
import org.example.sansam.product.dto.SearchStockRequest;
import org.example.sansam.product.dto.SearchStockResponse;
import org.example.sansam.product.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    // 상품 사이즈, 컬러 조회 시 대문자로 조회
    // 상품 상태값 처리
    @PatchMapping ("/{productId}/status")
    public ResponseEntity<?> changeStatus(@PathVariable Long productId) {
        try {
            ProductStatusResponse response = productService.checkProductStatus(productId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    //상품 재고 조회 - 품절 시 반환값
    @GetMapping("/search-stock")
    public ResponseEntity<?> searchStock(@RequestBody SearchStockRequest searchStockRequest) {
        try {
            SearchStockResponse response = productService.checkStock(searchStockRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    //상품 재고 추가, 차감
    @PostMapping ("/stock")
    public ResponseEntity<?> changeStock(@RequestBody ChangStockRequest changStockRequest) {
        try {
            SearchStockResponse response = productService.decreaseStock(changStockRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    //상품 상세 조회
    @GetMapping("/{productId}")
    public ResponseEntity<?> getDefaultOption(@PathVariable Long productId, @RequestParam(required = false) Long userId) {
        try {
            return ResponseEntity.ok(productService.getProduct(productId, userId));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    //상품 상세 조회 - 옵션 선택
    @GetMapping("/{productId}/option")
    public ResponseEntity<?> getColorOption(
            @PathVariable Long productId,
            @RequestParam String color
    ) {
        try {
            return ResponseEntity.ok(productService.getOptionByColor(productId, color));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

}
