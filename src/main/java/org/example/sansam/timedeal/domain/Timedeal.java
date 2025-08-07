package org.example.sansam.timedeal.domain;

import jakarta.persistence.*;
import lombok.*;
import org.example.sansam.product.domain.Product;
import org.example.sansam.product.domain.ProductDetail;
import org.example.sansam.product.domain.Status;

import java.time.LocalDateTime;

@Entity
@Table(name = "timedeal_products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Timedeal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "timedeal_id")
    private Long id;

    private Long price;

    @Column(name = "start_at")
    private LocalDateTime startAt;

    @Column(name = "end_at")
    private LocalDateTime endAt;



    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_detail_id", nullable = false)
    private ProductDetail productDetail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id", nullable = false)
    private Status status;
}
