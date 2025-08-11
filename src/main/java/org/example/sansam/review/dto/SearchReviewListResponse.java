package org.example.sansam.review.dto;

import lombok.*;
import org.example.sansam.review.domain.Review;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchReviewListResponse {
    private String userName;
    private String message;
    private int rating;
    private String url;

    public static SearchReviewListResponse from(Review review) {
        return SearchReviewListResponse.builder()
                .userName(review.getUser().getName())
                .message(review.getMessage())
                .rating(review.getStarRating())
                .url(review.getFile() != null ? review.getFile().getFileDetail().getUrl(): null)
                .build();
    }
}
