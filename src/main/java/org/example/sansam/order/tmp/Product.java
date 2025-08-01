package org.example.sansam.order.tmp;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Product {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    Long productId;

    private String name;
    private Long price;
    private int stockQuantity;

}
