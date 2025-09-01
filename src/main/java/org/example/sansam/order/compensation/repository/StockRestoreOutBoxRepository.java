package org.example.sansam.order.compensation.repository;

import org.example.sansam.order.compensation.domain.StockRestoreOutBox;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StockRestoreOutBoxRepository extends JpaRepository<StockRestoreOutBox, Long> {
    @Query("""
        select o from StockRestoreOutBox o
        where o.status = 'PENDING' and o.nextRunAt <= :now
        order by o.nextRunAt asc
    """)
    List<StockRestoreOutBox> findRunnable(@Param("now") LocalDateTime now, Pageable pageable);

    @Modifying
    @Transactional
    @Query("""
      update StockRestoreOutBox o
         set o.status='CLAIMED', o.lockedBy=:worker, o.lockedAt=:now, o.updatedAt=:now
       where o.id=:id and o.status='PENDING' and o.lockedBy is null
    """)
    int claim(@Param("id") Long id, @Param("worker") String worker, @Param("now") LocalDateTime now);
}