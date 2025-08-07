package org.example.sansam.timedeal.dto;

import lombok.*;
import org.example.sansam.product.dto.ProductDetailResponse;
import org.example.sansam.product.dto.ProductResponse;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TimeDealDetailResponse {
    private Long productId;
    private String productName;
    private String categoryName;
    private String brandName;
    private Long price;
    private String description;
    private String imageUrl;
    private ProductDetailResponse detailResponse;
    private Long timeDealPrice;
    private String timeDealStatus;
}
