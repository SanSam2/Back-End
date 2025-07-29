package org.example.sansam.wish.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeleteWishRequest {
    private Long userId;
    private Long productId;
}
