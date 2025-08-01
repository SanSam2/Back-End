package org.example.sansam.wish.domain;


import jakarta.persistence.*;
import lombok.*;
import org.example.sansam.product.domain.Product;
import org.example.sansam.user.domain.User;

@Entity
@Table(name = "wishes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wish {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Builder
    public Wish(User user, Product product) {
        this.user = user;
        this.product = product;
    }
}

