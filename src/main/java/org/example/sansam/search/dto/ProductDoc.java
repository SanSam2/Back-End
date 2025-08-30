package org.example.sansam.search.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductDoc {
    private Long productId;
    private String productName;
    private String brand;
    private Long price;
    private String url;
    private String bigCategory;
    private String middleCategory;
    private String smallCategory;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    private Long viewCount;
    private Long wishCount;
}
