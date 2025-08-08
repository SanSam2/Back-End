package org.example.sansam.order.dto;


import lombok.Getter;
import org.example.sansam.order.domain.OrderProduct;

@Getter
public class OrderProductSpec {

    private Long productId;
    private String productName;
    private Long productPrice;
    private int quantity;

    public OrderProductSpec(OrderProduct orderProduct) {
        this.productId = orderProduct.getProduct().getId();
        this.productName =orderProduct.getProduct().getProductName();
        this.productPrice = orderProduct.getProduct().getPrice();
        this.quantity = orderProduct.getQuantity();
    }
}
