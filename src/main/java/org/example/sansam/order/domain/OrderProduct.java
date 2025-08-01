package org.example.sansam.order.domain;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.sansam.order.tmp.Products;

@Entity
@Table(name = "order_product")
@Getter
@Setter
public class OrderProduct {

    @Id
    @Column(name="order_product_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderProductId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "products_id",nullable = false)
    private Products product;

    private Long quantity;
}
