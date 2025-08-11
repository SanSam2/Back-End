package org.example.sansam.cart.dto;
import lombok.*;
import org.example.sansam.product.dto.ProductResponse;
import org.example.sansam.search.dto.SearchListResponse;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchCartResponse {
    private SearchListResponse searchListResponse;
    private String size;
    private String color;
    private Long quantity; //수량
    private Long stock; //재고
}
