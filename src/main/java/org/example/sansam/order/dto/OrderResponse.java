package org.example.sansam.order.dto;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.sansam.order.domain.Order;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class OrderResponse {
    private Long orderId;
    private String orderNumber;
    private String orderName;
    private Long totalAmount;
    private String status;
    private List<OrderItemResponseDto> items;
    private LocalDateTime createdAt;
}
