package org.example.sansam.product.repository;

import org.example.sansam.product.domain.ProductConnect;
import org.example.sansam.product.domain.ProductOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductConnectJpaRepository extends JpaRepository<ProductConnect,Long> {
    List<ProductConnect> findByProductDetail_Id(Long productDetailId);

    @Query("SELECT p.option From ProductConnect p Where p.productDetail.id = :productDetailId")
    List<ProductOption> findOptionsByProductDetailId(Long productDetailId);
}
