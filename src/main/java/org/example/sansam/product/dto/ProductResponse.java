package org.example.sansam.product.dto;

import lombok.*;
import org.example.sansam.cart.dto.SearchCartResponse;

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
    private ProductDetailResponse option;
    private boolean wish;
    private Long reviewCount;
    private List<String> colorList;
    private List<String> sizeList;
    //사이즈
}