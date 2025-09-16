package org.example.sansam.search.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.sansam.search.dto.SearchItemResponse;
import org.example.sansam.search.dto.SearchListResponse;
import org.example.sansam.search.service.SearchService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/search")
@Slf4j
public class SearchController {
    private final SearchService searchService;

    @GetMapping("/brand")
    public ResponseEntity<?> searchListByBrand(@RequestParam(required = false) String brand,
                                               @RequestParam(required = false) Long userId,
                                               @RequestParam(required = false, defaultValue = "0") int page
    ) {
        try {
            log.info("searchListByBrand : {}", brand);
            List<SearchItemResponse> products = searchService.searchProductByBrand(
                    brand, userId, page);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }
    //상품 검색, 상품 상세 조회, 상품 추천 + 필터링
    //상품 검색,정렬
    @GetMapping("/keyword")
    public ResponseEntity<?> searchListByKeyword(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            @RequestParam(required = false, defaultValue = "createdAt") String sort
    ) {
        try {
            Page<SearchItemResponse> products = searchService.searchProductListByKeyword(
                    keyword, userId, page, size, sort);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @GetMapping("/category")
    public ResponseEntity<?> searchListByCategory(
            @RequestParam(required = false) String big,
            @RequestParam(required = false) String middle,
            @RequestParam(required = false) String small,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            @RequestParam(required = false, defaultValue = "createdAt") String sort
    ) {
        try {
            Page<SearchItemResponse> products = searchService.searchProductListByCategory(
                    big, middle, small, userId, page, size, sort);
            return ResponseEntity.ok(products);
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

    //---------------------------V1 Full Text Scan-------------------------
    @GetMapping("/keyword/v1")
    public ResponseEntity<?> searchListByKeywordV1(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "12") int size,
            @RequestParam(required = false, defaultValue = "createdAt") String sort
    ) {
        try {
            Page<SearchItemResponse> products = searchService.searchKeywordProductListV1(
                    keyword, userId, page, size, sort);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @GetMapping("/category/v1")
    public ResponseEntity<?> searchListByCategoryV1(
            @RequestParam(required = false) String big,
            @RequestParam(required = false) String middle,
            @RequestParam(required = false) String small,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "12") int size,
            @RequestParam(required = false, defaultValue = "createdAt") String sort
    ) {
        try {
            Page<SearchItemResponse> products = searchService.searchCategoryProductListV1(
                    big, middle, small, userId, page, size, sort);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }


    //상품 좋아요순 조회 - 메인
    @GetMapping("/like/v1")
    public ResponseEntity<?> getProductsByLikeV1(@RequestParam(required = false) Long userId) {
        try {
            List<SearchListResponse> products = searchService.getProductsByLikeV1(userId);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    //상품 추천순 조회 - 메인
    @GetMapping("/recommend/v1")
    public ResponseEntity<?> getProductsByRecommendV1(@RequestParam(required = false) Long userId) {
        try {
            List<SearchListResponse> products = searchService.getProductsByRecommendV1(userId);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    //---------------------------V2 쿼리 개선 및 DB 인덱스 조회-------------------------
    @GetMapping("/keyword/v2")
    public ResponseEntity<?> searchListByKeywordV2(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "12") int size,
            @RequestParam(required = false, defaultValue = "createdAt") String sort
    ) {
        try {
            Page<SearchItemResponse> products = searchService.searchKeywordProductListV2(
                    keyword, userId, page, size, sort);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @GetMapping("/category/v2")
    public ResponseEntity<?> searchListByCategory2(
            @RequestParam(required = false) String big,
            @RequestParam(required = false) String middle,
            @RequestParam(required = false) String small,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "12") int size,
            @RequestParam(required = false, defaultValue = "createdAt") String sort
    ) {
        try {
            Page<SearchItemResponse> products = searchService.searchCategoryProductListV2(
                    big, middle, small, userId, page, size, sort);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }


    //상품 좋아요순 조회 - 메인
    @GetMapping("/like/v2")
    public ResponseEntity<?> getProductsByLikeV2(@RequestParam(required = false) Long userId) {
        try {
            List<SearchListResponse> products = searchService.getProductsByLikeV2(userId);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    //상품 추천순 조회 - 메인
    @GetMapping("/recommend/v2")
    public ResponseEntity<?> getProductsByRecommendV2(@RequestParam(required = false) Long userId) {
        try {
            List<SearchListResponse> products = searchService.getProductsByRecommendV2(userId);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }


    //---------------------------V3 Query DSL-------------------------


    //---------------------------V5 ES + Redis caching 사용-------------------------
    @GetMapping("/keyword/v5")
    public ResponseEntity<?> searchListByKeywordV5(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "12") int size,
            @RequestParam(required = false, defaultValue = "createdAt") String sort
    ) {
        try {
            Page<SearchItemResponse> products = searchService.searchKeywordProductListV2(
                    keyword, userId, page, size, sort);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @GetMapping("/category/v5")
    public ResponseEntity<?> searchListByCategoryV5(
            @RequestParam(required = false) String big,
            @RequestParam(required = false) String middle,
            @RequestParam(required = false) String small,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "12") int size,
            @RequestParam(required = false, defaultValue = "createdAt") String sort
    ) {
        try {
            Page<SearchItemResponse> products = searchService.searchCategoryProductListV2(
                    big, middle, small, userId, page, size, sort);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }


    //상품 좋아요순 조회 - 메인
    @GetMapping("/like/v5")
    public ResponseEntity<?> getProductsByLikeV5(@RequestParam(required = false) Long userId) {
        try {
            List<SearchListResponse> products = searchService.getProductsByLikeV2(userId);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    //상품 추천순 조회 - 메인
    @GetMapping("/recommend/v5")
    public ResponseEntity<?> getProductsByRecommendV5(@RequestParam(required = false) Long userId) {
        try {
            List<SearchListResponse> products = searchService.getProductsByRecommendV2(userId);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }
}
