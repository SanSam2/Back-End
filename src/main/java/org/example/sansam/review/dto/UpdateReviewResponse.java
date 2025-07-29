package org.example.sansam.review.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateReviewResponse {
    private String message;
    private int rating;
    private String url;
}
