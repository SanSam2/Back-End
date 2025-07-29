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
    private String status;
    private Long originalPrice;
    private Long timeDealPrice;
    private LocalDateTime startAt;
    private String url;
}
