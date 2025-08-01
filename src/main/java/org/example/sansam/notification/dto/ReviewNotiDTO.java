package org.example.sansam.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReviewNotiDTO {
    private Long productId;
    private String productName;
}