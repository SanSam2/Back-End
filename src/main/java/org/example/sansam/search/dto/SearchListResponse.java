package org.example.sansam.search.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchListResponse {
    private Long productId;
    private String productName;
    private int price;
    private String url;
    private boolean wish;
}
