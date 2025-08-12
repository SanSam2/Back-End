package org.example.sansam.review.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.example.sansam.product.dto.TextResponse;
import org.example.sansam.review.dto.*;
import org.example.sansam.review.service.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;
    //리뷰 작성, 리뷰 수정, 리뷰 삭제, 리뷰 전체 조회
    // 리뷰 작성
    @Operation(summary = "리뷰 생성", description = "새로운 리뷰를 생성합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AddReviewRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                        "userId":1,
                                        "productId":4,
                                        "orderNumber": 12343243,
                                        "message":"좋아요",
                                        "rating":5,
                                        "url": "https://sansam2-bucket.s3.ap-northeast-2.amazonaws.com/images/25fab023-3052-4634-8236-c92898265582.img2.jpg",
                                        "size": 5.5
                                    }
            """)
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "리뷰 생성 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TextResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                        "message": "리뷰 추가 완료"
                                    }
                                """))),
            @ApiResponse(responseCode = "400", description = "유효성 오류"),
            @ApiResponse(responseCode = "404", description = "사용자 또는 상품 없음")
    })
    @PostMapping("/create")
    public ResponseEntity<?> createReview(@RequestBody AddReviewRequest addReviewRequest) {
        try{
            reviewService.createReview(addReviewRequest);
            return ResponseEntity.ok(new TextResponse("리뷰 추가 완료"));
        }catch(IllegalArgumentException e){
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    // 리뷰 수정
    @Operation(summary = "리뷰 수정", description = "userId와 orderNumber를 기준으로 리뷰 내용을 수정합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UpdateReviewRequest.class),
                    examples = @ExampleObject(value = """
                                    {
                                        "userId":1,
                                        "productId":4,
                                        "orderNumber": 12343243,
                                        "message":"무난하네요",
                                        "rating":4,
                                        "url": "https://sansam2-bucket.s3.ap-northeast-2.amazonaws.com/images/25fab023-3052-4634-8236-c92898265582.img2.jpg",
                                        "size": 5.5
                                    }
                    """)
                )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "리뷰 수정 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UpdateReviewResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                         "message": "ㅇㅇㅇㅇㅇ",
                                         "starRating": 5,
                                         "url": "https://sansam2-bucket.s3.ap-northeast-2.amazonaws.com/images/063e2c49-16ff-4221-85a0-961e74f87bb9.test222.jpg"
                                    }
                                """))),
            @ApiResponse(responseCode = "404", description = "리뷰 없음"),
            @ApiResponse(responseCode = "400", description = "유효성 오류")
    })
    @PatchMapping("/update")
    public ResponseEntity<?> updateReview(@RequestBody UpdateReviewRequest updateReviewRequest) {
        try {
            UpdateReviewResponse response = reviewService.updateReview(updateReviewRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    // 리뷰 삭제
    @Operation(summary = "리뷰 삭제", description = "userId와 orderNumber를 기준으로 리뷰를 삭제합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DeleteReviewRequest.class),
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
            @ApiResponse(responseCode = "204", description = "리뷰 삭제 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                        "message": "리뷰 삭제 완료"
                                    }
                                """))),
            @ApiResponse(responseCode = "404", description = "리뷰 없음")
    })
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteReview(@RequestBody DeleteReviewRequest deleteReviewRequest) {
        try{
            reviewService.deleteReview(deleteReviewRequest);
            return ResponseEntity.ok(new TextResponse("리뷰 삭제 완료"));
        }catch(IllegalArgumentException e){
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }


    // 상품의 리뷰 리스트 조회
    @Operation(summary = "상품 리뷰 목록 조회", description = "상품 ID를 기준으로 리뷰 목록을 조회합니다.",
            parameters = {
                    @Parameter(name = "productId", in = ParameterIn.PATH, required = true,
                            description = "상품 ID", example = "1")
            }
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "리뷰 목록 조회 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                {
                                  "content": 
                                    [
                                        {
                                            "userName": "유저1",
                                            "message": "좋아요",
                                            "rating": 5,
                                            "url": "https://sansam2-bucket.s3.ap-northeast-2.amazonaws.com/images/25fab023-3052-4634-8236-c92898265582.img2.jpg",
                                            "productId": 1
                                        },
                                        {
                                            "userName": "유저2",
                                            "message": "배송이 빠르네요",
                                            "rating": 5,
                                            "url": "https://sansam2-bucket.s3.ap-northeast-2.amazonaws.com/images/25fab023-3052-4634-8236-c92898265582.img3.jpg",
                                            "productId": 1
                                        }
                                    ]
                                }
                                """)))
    })
    @GetMapping("/product/{productId}")
    public ResponseEntity<?> getReviewsByProduct(@PathVariable Long productId) {
        try {
            List<SearchReviewListResponse> reviews = reviewService.searchReviews(productId);
            return ResponseEntity.ok(reviews);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }


    @Operation(summary = "나의 리뷰 목록 조회", description = "userID를 기준으로 자신이 작성한 리뷰 목록을 조회합니다.",
            parameters = {
                    @Parameter(name = "userId", in = ParameterIn.PATH, required = true,
                            description = "유저 ID", example = "1")
            }
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "리뷰 목록 조회 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                {
                                  "content":
                                    [
                                        {
                                            "userName": "유저1",
                                            "message": "좋아요 추천",
                                            "rating": 5,
                                            "url": "https://sansam2-bucket.s3.ap-northeast-2.amazonaws.com/images/063e2c49-16ff-4221-85a0-961e74f87bb9.test222.jpg",
                                            "productId": 5
                                        },
                                        {
                                            "userName": "유저2",
                                            "message": "추천합니다",
                                            "rating": 5,
                                            "url": "https://sansam2-bucket.s3.ap-northeast-2.amazonaws.com/images/25fab023-3052-4634-8236-c92898265582.img2.jpg",
                                            "productId": 4
                                        }
                                    ]
                                }
                               """)))
    })
    @GetMapping("/mypage")
    public ResponseEntity<?> getMyReviews(@RequestParam Long userId) {
        try {
            List<SearchReviewListResponse> reviews = reviewService.searchMyReviews(userId);
            return ResponseEntity.ok(reviews);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }
}
