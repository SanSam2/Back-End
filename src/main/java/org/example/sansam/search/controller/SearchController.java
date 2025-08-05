package org.example.sansam.search.controller;


import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.sansam.product.dto.ProductResponse;
import org.example.sansam.search.dto.RecommendRequest;
import org.example.sansam.search.dto.SearchListResponse;
import org.example.sansam.search.service.SearchService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/search")
public class SearchController {
    private final SearchService searchService;
    //상품 검색, 상품 상세 조회, 상품 추천
    //상품 검색,정렬
    @GetMapping
    public ResponseEntity<?> searchList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            @RequestParam(required = false, defaultValue = "createdAt") String sort
    ) {
        try {
            Page<SearchListResponse> products = searchService.searchProductList(keyword, category, userId, page, size, sort);
            return ResponseEntity.ok(products.getContent());
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    //상품 좋아요순 조회 - 메인
    @GetMapping("/like")
    public ResponseEntity<?> getProductsByLike(@RequestParam(required = false) Long userId) {
        try {
            List<SearchListResponse> products = searchService.getProductsByLike(userId);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    //상품 추천순 조회 - 메인
    @GetMapping("/recommend")
    public ResponseEntity<?> getProductsByRecommend(@RequestParam(required = false) Long userId) {
        try {
            List<SearchListResponse> products = searchService.getProductsByRecommend(userId);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

}
