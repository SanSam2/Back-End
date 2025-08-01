package org.example.sansam.order.domain;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.sansam.product.domain.Product;

@Entity
@Table(name = "order_product")
@Getter
@Setter
public class OrderProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id",nullable = false)
    private Product product;

    private Long quantity;
}
