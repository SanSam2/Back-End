package org.example.sansam.product.repository;

import org.example.sansam.product.domain.ProductConnect;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductConnectJpaRepository extends JpaRepository<ProductConnect,Long> {
}
