package org.example.sansam.review.domain;

import jakarta.persistence.*;
import lombok.*;
import org.example.sansam.product.domain.Product;
import org.example.sansam.s3.domain.FileManagement;
import org.example.sansam.user.domain.User;

@Entity
@Table(name = "reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reviews_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_management_id")
    private FileManagement file;

    @Column(nullable = false)
    private String message;

    @Column(name = "star_rating", nullable = false)
    private Integer starRating;
}