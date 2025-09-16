package org.example.sansam.product.dto;

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
    private Long price;
    private String description;
    private String imageUrl;
    private String status;
    private ProductDetailResponse detailResponse;
    private boolean wish;
    private Long reviewCount;
    private List<String> colorList;
    private List<String> sizeList;
}