package org.example.sansam.review.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class DeleteReviewRequest {
    private Long userId;
    private Long productId;
}
