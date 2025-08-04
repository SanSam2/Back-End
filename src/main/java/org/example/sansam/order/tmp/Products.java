package org.example.sansam.order.tmp;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "tmpproducts")
public class Products {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="tmpproducts_id")
    Long productsId;

    private String name;
    private Long price;
    private int stockQuantity;

}
