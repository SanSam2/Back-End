package org.example.sansam.product.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "product_detail")
@Getter
@NoArgsConstructor
public class ProductDetail {
    @Id

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    private Long quantity;

    @Column(name = "map_name")
    private String mapName;

    @Column(name = "file_management_id", nullable = false)
    private Long fileManagementId;

    @OneToMany(mappedBy = "productDetail", cascade = CascadeType.ALL)
    private List<ProductConnect> productConnects = new ArrayList<>();
}
