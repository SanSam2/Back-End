package org.example.sansam.product.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProducStatusResponse {
    private String productName;
    private String productStatus;
}
