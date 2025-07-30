package org.example.sansam.search.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {
    private Long productId;
    private String productName;
    private String categoryName;
    private String brandName;
    private Integer price;
    private String description;
    private String imageUrl;
    private List<ProductDetailResponse> options;
    private boolean wish;
}