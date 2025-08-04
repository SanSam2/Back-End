package org.example.sansam.order.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class OrderRequest {

    private Long userId;
    private List<OrderItemDto> items;
}
