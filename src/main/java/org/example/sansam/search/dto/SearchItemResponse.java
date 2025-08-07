package org.example.sansam.search.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchItemResponse {
    private Long productId;
    private String productName;
    private Long price;
    private String url;
    private boolean wish;
    private String category;
}
