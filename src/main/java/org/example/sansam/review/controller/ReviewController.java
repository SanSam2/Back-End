package org.example.sansam.review.controller;

import lombok.RequiredArgsConstructor;
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
    @PostMapping("/create")
    public ResponseEntity<?> createReview(@RequestBody AddReviewRequest addReviewRequest) {
        try{
            reviewService.createReview(addReviewRequest);
            return ResponseEntity.ok("리뷰 추가 완료");
        }catch(IllegalArgumentException e){
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    // 리뷰 수정
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
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteReview(@RequestBody DeleteReviewRequest deleteReviewRequest) {
        try{
            reviewService.deleteReview(deleteReviewRequest);
            return ResponseEntity.ok("리뷰 삭제 완료");
        }catch(IllegalArgumentException e){
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    // 상품의 리뷰 리스트 조회
    @GetMapping("/product/{productId}")
    public ResponseEntity<?> getReviewsByProduct(@PathVariable Long productId) {
        try {
            List<SearchReviewListResponse> reviews = reviewService.searchReviews(productId);
            return ResponseEntity.ok(reviews);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }
}
