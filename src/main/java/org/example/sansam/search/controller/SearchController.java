package org.example.sansam.search.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.example.sansam.search.dto.SearchItemResponse;
import org.example.sansam.search.dto.SearchListResponse;
import org.example.sansam.search.service.SearchService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/search")
public class SearchController {
    private final SearchService searchService;

    //상품 검색, 상품 상세 조회, 상품 추천
    //상품 검색,정렬
    @Operation(
            summary = "상품 검색",
            description = "키워드, 카테고리, 정렬, 페이지네이션 조건으로 상품 목록을 조회합니다.",
            parameters = {
                    @Parameter(name = "keyword", in = ParameterIn.QUERY, required = false,
                            description = "검색 키워드 (상품명/카테고리명 부분검색)", example = "구찌"),
                    @Parameter(name = "big", in = ParameterIn.QUERY, required = false,
                            description = "대분류 카테고리명", example = "여성"),
                    @Parameter(name = "middle", in = ParameterIn.QUERY, required = false,
                            description = "중분류 카테고리명", example = "상의"),
                    @Parameter(name = "small", in = ParameterIn.QUERY, required = false,
                            description = "소분류 카테고리명", example = "반팔"),
                    @Parameter(name = "userId", in = ParameterIn.QUERY, required = false,
                            description = "사용자 ID (위시리스트 여부/개인화 추천에 사용)", example = "1"),
                    @Parameter(name = "page", in = ParameterIn.QUERY, required = false,
                            description = "페이지 번호 (0부터 시작)", example = "0"),
                    @Parameter(name = "size", in = ParameterIn.QUERY, required = false,
                            description = "페이지 크기", example = "10"),
                    @Parameter(name = "sort", in = ParameterIn.QUERY, required = false,
                            description = "정렬 기준 필드명", example = "createdAt")
            }
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상품 조회 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                        "content": [
                                            {
                                                "productId": 32,
                                                "productName": "샤넬 반팔티 701",
                                                "price": 800000,
                                                "url": "https://sansam2-bucket.s3.ap-northeast-2.amazonaws.com/images/25fab023-3052-4634-8236-c92898265582.img2.jpg",
                                                "wish": false,
                                                "category": "여성>상의>반팔"
                                            },
                                            {
                                                "productId": 29,
                                                "productName": "샤넬 반바지 401",
                                                "price": 1200000,
                                                "url": "https://sansam2-bucket.s3.ap-northeast-2.amazonaws.com/images/25fab023-3052-4634-8236-c92898265582.img2.jpg",
                                                "wish": false,
                                                "category": "여성>하의>반바지"
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
                                        "totalElements": 2,
                                        "totalPages": 1,
                                        "first": true,
                                        "size": 10,
                                        "number": 0,
                                        "sort": {
                                            "empty": false,
                                            "sorted": true,
                                            "unsorted": false
                                        },
                                        "numberOfElements": 2,
                                        "empty": false
                                    }
                     """)))
    })
    @GetMapping
    public ResponseEntity<?> searchList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String big,
            @RequestParam(required = false) String middle,
            @RequestParam(required = false) String small,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            @RequestParam(required = false, defaultValue = "createdAt") String sort
    ) {
        try {
            Page<SearchItemResponse> products = searchService.searchProductList(
                    keyword, big, middle, small, userId, page, size, sort);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    //상품 좋아요순 조회 - 메인

    @Operation(
            summary = "상품 좋아요순 리스트",
            description = "상품 좋아요순으로 정렬합니다. ",
            parameters = {
                    @Parameter(name = "userId", in = ParameterIn.PATH, required = true,
                            description = "유저 ID", example = "1")
            }
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상품 좋아요 순 조회 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    [
                                        {
                                            "productId": 4,
                                            "productName": "하트 긴팔티",
                                            "price": 50000,
                                            "url": "https://sansam2-bucket.s3.ap-northeast-2.amazonaws.com/images/25fab023-3052-4634-8236-c92898265582.img2.jpg",
                                            "wish": false
                                        },
                                        {
                                            "productId": 14,
                                            "productName": "구찌 반팔티",
                                            "price": 500000,
                                            "url": "https://sansam2-bucket.s3.ap-northeast-2.amazonaws.com/images/25fab023-3052-4634-8236-c92898265582.img2.jpg",
                                            "wish": false
                                        }....
                                    ]
                     """)))
    })
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
    @Operation(
            summary = "상품 추천순 리스트",
            description = "상품 추천순으로 정렬합니다. ",
            parameters = {
                    @Parameter(name = "userId", in = ParameterIn.PATH, required = true,
                            description = "유저 ID", example = "1")
            }
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상품 추천순 조회 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    [
                                        {
                                            "productId": 4,
                                            "productName": "하트 긴팔티",
                                            "price": 50000,
                                            "url": "https://sansam2-bucket.s3.ap-northeast-2.amazonaws.com/images/25fab023-3052-4634-8236-c92898265582.img2.jpg",
                                            "wish": false
                                        },
                                        {
                                            "productId": 14,
                                            "productName": "구찌 반팔티",
                                            "price": 500000,
                                            "url": "https://sansam2-bucket.s3.ap-northeast-2.amazonaws.com/images/25fab023-3052-4634-8236-c92898265582.img2.jpg",
                                            "wish": false
                                        }....
                                    ]
                     """)))
    })
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
