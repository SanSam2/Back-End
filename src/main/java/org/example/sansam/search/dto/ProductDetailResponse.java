package org.example.sansam.search.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDetailResponse {
    private Long id;
    private String type;
    private String name;
}