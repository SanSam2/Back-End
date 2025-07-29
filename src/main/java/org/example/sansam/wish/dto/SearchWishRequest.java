package org.example.sansam.wish.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchWishRequest {
    private Long userId;
    private int page;
    private int size;
}