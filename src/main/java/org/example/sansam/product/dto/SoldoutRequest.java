package org.example.sansam.product.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SoldoutRequest {
    private Long productId;
    private String size;
    private String color;
}
