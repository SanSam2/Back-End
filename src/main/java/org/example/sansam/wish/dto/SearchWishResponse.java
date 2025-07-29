package org.example.sansam.wish.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchWishResponse {
    private Long productId;
    private String productName;
    private String url;
}
