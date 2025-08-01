package org.example.sansam.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.sansam.order.domain.Order;
import org.example.sansam.product.domain.Product;
import org.example.sansam.user.domain.User;

@Data
@AllArgsConstructor
public class ReviewRequestDTO {
    private User user;
    private Order order;
    private Product product;
}
