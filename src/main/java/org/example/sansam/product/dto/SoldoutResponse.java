package org.example.sansam.product.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SoldoutResponse {
    private String productName;
    private String size;
    private String color;
    private String productStatus;
}
