package org.example.sansam.timedeal.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TimeDealResponse {
    private Long productId;
    private String productName;
    private String productImage;
    private String status;
    private Long originalPrice;
    private Long timeDealPrice;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
}
