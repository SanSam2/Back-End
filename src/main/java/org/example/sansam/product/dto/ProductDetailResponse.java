package org.example.sansam.product.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDetailResponse {
    private String color;
    private String url;
    private List<OptionResponse> options;
}