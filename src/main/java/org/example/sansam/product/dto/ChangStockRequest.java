package org.example.sansam.product.dto;

import lombok.Getter;
import lombok.Setter;
import org.example.sansam.product.domain.ProductStatus;

@Getter
@Setter
public class ChangStockRequest {
    private Long productId;
    private String size;
    private String color;
    private ProductStatus status;
    private int num;
}
