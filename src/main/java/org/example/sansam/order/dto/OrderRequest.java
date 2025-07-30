package org.example.sansam.order.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class OrderRequest {

    private String userEmail;
    private List<OrderItemDto> items;
    private String paymentKey; // Toss에서 받은 값
}
