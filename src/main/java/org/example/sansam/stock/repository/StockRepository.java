package org.example.sansam.stock.repository;

import org.example.sansam.stock.domain.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {
    // 상품 1건에 대한 디테일 재고
    Optional<Stock> findByProductDetailsId(Long productDetailsId);

}
