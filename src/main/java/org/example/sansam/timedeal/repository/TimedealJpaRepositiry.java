package org.example.sansam.timedeal.repository;

import org.example.sansam.timedeal.domain.Timedeal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TimedealJpaRepositiry extends JpaRepository<Timedeal, Long> {
    @Query("SELECT t FROM Timedeal t where t.startAt > :start AND t.startAt < :end")
    List<Timedeal> findByStartAtBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT t FROM Timedeal t where t.startAt = :start AND t.productDetail.product.id = :productId")
    Timedeal findByStartAt(@Param("start") LocalDateTime start, @Param("productId") Long productId);
}
