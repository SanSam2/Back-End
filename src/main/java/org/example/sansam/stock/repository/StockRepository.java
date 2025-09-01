package org.example.sansam.stock.repository;

import org.example.sansam.stock.domain.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {
    // 상품 1건에 대한 디테일 재고
    Optional<Stock> findByProductDetailsId(Long productDetailsId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE Stock s 
           SET s.stockQuantity = s.stockQuantity - :qty
         WHERE s.productDetailsId = :detailId
           AND s.stockQuantity >= :qty
        """)
    int decreaseIfEnough(@Param("detailId") Long detailId, @Param("qty") int qty);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE Stock s
           SET s.stockQuantity = s.stockQuantity + :qty
         WHERE s.productDetailsId = :detailId
        """)
    int increase(@Param("detailId") Long detailId, @Param("qty") int qty);
}
