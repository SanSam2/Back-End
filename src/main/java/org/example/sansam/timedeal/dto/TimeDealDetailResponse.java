package org.example.sansam.timedeal.dto;

import lombok.*;
import org.example.sansam.product.dto.ProductResponse;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TimeDealDetailResponse {
    private ProductResponse product;
    private Long timeDealPrice;
    private String status;
}
