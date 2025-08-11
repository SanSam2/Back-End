package org.example.sansam.review.dto;

import lombok.*;
import org.example.sansam.review.domain.Review;
import org.example.sansam.s3.domain.FileManagement;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateReviewResponse {
    private String message;
    private int starRating;
    private String url;

    public static UpdateReviewResponse from(Review review) {
        return UpdateReviewResponse.builder()
                .message(review.getMessage())
                .starRating(review.getStarRating())
                .build();
    }
}
