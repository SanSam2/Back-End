package org.example.sansam.product.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    // 상품 사이즈, 컬러 조회 시 대문자로 조회
    // 상품 상태값 처리
    @Operation(summary = "상품 상태 변경", description = "상품 ID를 기준으로 상품 상태값을 변경합니다.",
            parameters = {
                    @Parameter(name = "productId", in = ParameterIn.PATH, required = true,
                            description = "사용자 ID", example = "1")
            }
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상태 조회 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                        "productName": "검은 긴팔티",
                                        "productStatus": "NEW"
                                    }
                                """)))
    })
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
    @Operation(summary = "재고 조회", description = "옵션 별 재고를 조회합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SearchStockRequest.class),
                            examples = @ExampleObject(value = """
                                {
                                       "productId":4,
                                       "size":"s",
                                       "color":"white"
                                }
                    """)
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "재고 조회 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                   {
                                             "productId": 4,
                                             "size": "S",
                                             "color": "WHITE",
                                             "quantity": 55
                                   }
                                """)))
    })
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

//    @PostMapping ("/stock")
//    public ResponseEntity<?> changeStock(@RequestBody ChangStockRequest changStockRequest) {
//        try {
//            SearchStockResponse response = productService.decreaseStock(changStockRequest);
//            return ResponseEntity.ok(response);
//        } catch (Exception e) {
//            return ResponseEntity.status(400).body(e.getMessage());
//        }
//    }

    //상품 상세 조회
    @Operation(summary = "상품 상세 조회", description = "상품 ID로 상품 상세 정보를 조회합니다.",
            parameters = {
                    @Parameter(name = "productId", in = ParameterIn.PATH, required = true,
                            description = "상품 ID", example = "1"),
                    @Parameter(name = "userId", in = ParameterIn.PATH, required = false,
                            description = "유저 ID", example = "1")
            }
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상품 상세 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProductResponse.class),
                            examples = @ExampleObject(value = """
                                {
                                         "productId": 4,
                                         "productName": "하트 긴팔티",
                                         "categoryName": "여성>상의>긴팔",
                                         "brandName": "구찌",
                                         "price": 50000,
                                         "description": ".",
                                         "imageUrl": "https://sansam2-bucket.s3.ap-northeast-2.amazonaws.com/images/25fab023-3052-4634-8236-c92898265582.img2.jpg",
                                         "status": "NEW",
                                         "detailResponse": {
                                             "color": "WHITE",
                                             "url": "https://sansam2-bucket.s3.ap-northeast-2.amazonaws.com/images/25fab023-3052-4634-8236-c92898265582.img2.jpg",
                                             "options": [
                                                 {
                                                     "size": "S",
                                                     "quantity": 55
                                                 }
                                             ]
                                         },
                                         "wish": true,
                                         "reviewCount": 2,
                                         "colorList": [
                                             "WHITE"
                                         ],
                                         "sizeList": [
                                             "S"
                                         ]
                                     }
                     """)))
    })
    @GetMapping("/{productId}")
    public ResponseEntity<?> getDefaultOption(@PathVariable Long productId, @RequestParam(required = false) Long userId) {
        try {
            return ResponseEntity.ok(productService.getProduct(productId, userId));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    //상품 상세 조회 - 옵션 선택
    @Operation(summary = "상품 옵션별 조회", description = "상품 ID와 color로 상품 상세 정보를 조회합니다.",
            parameters = {
                    @Parameter(name = "productId", in = ParameterIn.PATH, required = true,
                            description = "상품 ID", example = "1"),
                    @Parameter(name = "color", in = ParameterIn.QUERY, required = true,
                            description = "color", example = "BLACK")
            }
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상품 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProductDetailResponse.class),
                            examples = @ExampleObject(value = """
                                {
                                       "color": "WHITE",
                                       "url": "https://sansam2-bucket.s3.ap-northeast-2.amazonaws.com/images/25fab023-3052-4634-8236-c92898265582.img2.jpg",
                                       "options": [
                                            {
                                                 "size": "S",
                                                 "quantity": 55
                                            }
                                       ]
                                }
                     """)))
    })
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
