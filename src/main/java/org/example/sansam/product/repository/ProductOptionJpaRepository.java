package org.example.sansam.product.repository;

import org.example.sansam.product.domain.ProductOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductOptionJpaRepository extends JpaRepository<ProductOption,Long> {

}
