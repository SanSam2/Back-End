package org.example.sansam.product.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchStockResponse {
    private Long productId;
    private String size;
    private String color;
    private String productStatus;
    private int quantity;
}
