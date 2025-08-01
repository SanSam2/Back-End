package org.example.sansam.order.tmp;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Products {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="products_id")
    Long productsId;

    private String name;
    private Long price;
    private int stockQuantity;

}
