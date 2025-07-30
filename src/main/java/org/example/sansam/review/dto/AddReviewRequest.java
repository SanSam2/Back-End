package org.example.sansam.review.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddReviewRequest {
    private Long userId;
    private Long productId;
    private String message;
    private int rating;
    private String url;
}
