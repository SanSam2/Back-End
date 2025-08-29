package org.example.sansam.product.domain;

import jakarta.persistence.*;
import lombok.*;
import org.example.sansam.review.domain.Review;
import org.example.sansam.s3.domain.FileManagement;
import org.example.sansam.status.domain.Status;
import org.example.sansam.wish.domain.Wish;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "brand_name", nullable = false)
    private String brandName;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id", nullable = false)
    private Status status;

    private Long price;

    @Column(name = "view_count")
    private Long viewCount;

    @Column(columnDefinition = "text")
    private String description;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_management_id")
    private FileManagement fileManagement;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<Review> reviewList;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<ProductDetail> productDetails;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<Wish> wishList;
}