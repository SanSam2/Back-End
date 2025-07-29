package org.example.sansam.cart.dto;
import lombok.*;
import org.example.sansam.search.dto.ProductResponse;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchCartResponse {
    private ProductResponse productResponse;
    private String size;
    private String color;
    private Long quantity;
    private String status; // 품절 상태
}
