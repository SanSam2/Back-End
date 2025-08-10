package org.example.sansam.product.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangStockRequest {
    private Long productId;
    private String size;
    private String color;
    private int num;
}
