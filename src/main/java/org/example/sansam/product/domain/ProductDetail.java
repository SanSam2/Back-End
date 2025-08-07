package org.example.sansam.product.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.sansam.product.dto.Option;
import org.example.sansam.s3.domain.FileManagement;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "product_detail")
@Getter
@Setter
@NoArgsConstructor
public class ProductDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_detail_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    private Long quantity;

    @Column(name = "map_name")
    private String mapName;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_management_id")
    private FileManagement fileManagement;

    @OneToMany(mappedBy = "productDetail", cascade = CascadeType.ALL)
    private List<ProductConnect> productConnects = new ArrayList<>();

    public Option getOptionName() {
        String color = null;
        String size = null;
        for (ProductConnect connect : productConnects) {
            ProductOption option = connect.getOption();
            if (option.getType().equals("color") ) {
                color = option.getName();
            } else if (option.getType().equals("size")) {
                size = option.getName();
            }
        }
        System.out.println(color + " " + size);
        return new Option(color, size);
    }


}
