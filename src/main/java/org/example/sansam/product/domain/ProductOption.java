package org.example.sansam.product.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "product_option")
@Getter
@NoArgsConstructor
public class ProductOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50, nullable = false)
    private String type;

    @Column(length = 255)
    private String name;

    @Column(name = "use_yn", nullable = false)
    private Boolean useYn;

    @Column(name = "option_date", nullable = false)
    private LocalDateTime optionDate;

    @OneToMany(mappedBy = "option", cascade = CascadeType.ALL)
    private List<ProductConnect> productConnects = new ArrayList<>();
}
