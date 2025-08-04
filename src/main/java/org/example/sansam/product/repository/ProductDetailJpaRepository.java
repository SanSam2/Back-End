package org.example.sansam.product.repository;

import org.example.sansam.product.domain.Product;
import org.example.sansam.product.domain.ProductDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductDetailJpaRepository extends JpaRepository<ProductDetail,Long> {
    List<ProductDetail> findByProduct(Product product);
}
