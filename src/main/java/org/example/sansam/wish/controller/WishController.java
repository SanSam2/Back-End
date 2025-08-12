package org.example.sansam.wish.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.example.sansam.cart.dto.AddCartRequest;
import org.example.sansam.wish.dto.AddWishRequest;
import org.example.sansam.wish.dto.DeleteWishRequest;
import org.example.sansam.wish.dto.SearchWishResponse;
import org.example.sansam.product.dto.TextResponse;
import org.example.sansam.wish.service.WishService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/wishes")
public class WishController {
    private final WishService wishService;

    //위시 추가, 삭제, 리스트 조회,
    @Operation(summary = "위시 추가", description = "위시리스트에 상품을 추가합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AddCartRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                        "userId":1,
                                        "productId":4
                                    }
                            """)
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "위시 추가 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TextResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                        "message": "위시 추가 성공"
                                    }
                                """))),
            @ApiResponse(responseCode = "400", description = "유효성 오류"),
            @ApiResponse(responseCode = "404", description = "사용자 또는 상품 없음")
    })
    @PostMapping("/add")
    public ResponseEntity<?> addWish(@RequestBody AddWishRequest addWishRequest) {
        try{
            wishService.addWish(addWishRequest);
            return ResponseEntity.ok(new TextResponse("위시 추가 성공"));
        } catch(Exception e){
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @Operation(summary = "위시 상품 삭제", description = "위시리스트에서 상품을 삭제합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DeleteWishRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                        "deleteWishItemList" :[
                                            {
                                                "userId":1,
                                                "productId":3
                                            }
                                        ]
                                    }
                    """)
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "위시리스트 상품 삭제 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                        "message": "위시 삭제 완료"
                                    }
                                """))),
            @ApiResponse(responseCode = "404", description = "위시 상품 없음")
    })
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteWish(@RequestBody DeleteWishRequest deleteWishRequest) {
        try{
            wishService.deleteWish(deleteWishRequest);
            return ResponseEntity.ok(new TextResponse("위시 삭제 성공"));
        }catch(Exception e){
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @Operation(summary = "위시리스트 조회", description = "유저 ID로 상품 상세 정보를 조회합니다.",
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
            @ApiResponse(responseCode = "200", description = "위시 목록 조회 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                        "content": [
                                            {
                                                "productId": 4,
                                                "productName": "하트 긴팔티",
                                                "url": "https://sansam2-bucket.s3.ap-northeast-2.amazonaws.com/images/25fab023-3052-4634-8236-c92898265582.img2.jpg"
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
    @GetMapping("/list")
    public ResponseEntity<?> searchWishList(
            @RequestParam Long userId,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {
        try {
            Page<SearchWishResponse> wishList = wishService.searchWishList(userId, page, size);
            return ResponseEntity.ok(wishList);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }
}
