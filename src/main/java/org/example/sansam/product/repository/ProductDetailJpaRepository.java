package org.example.sansam.product.repository;

import org.example.sansam.product.domain.Product;
import org.example.sansam.product.domain.ProductDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductDetailJpaRepository extends JpaRepository<ProductDetail,Long> {
    List<ProductDetail> findByProduct(Product product);

    @Query("""
    select distinct pd from ProductDetail pd
    left join fetch pd.productConnects pc
    left join fetch pc.option
    where pd.product = :product
""")
    List<ProductDetail> findByProductWithConnects(@Param("product") Product product);

}
