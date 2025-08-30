package org.example.sansam.payment.compensation.repository;

import org.example.sansam.payment.compensation.domain.PaymentCancelOutBox;
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
public interface PaymentCancelOutBoxRepository extends JpaRepository<PaymentCancelOutBox, Long> {

    @Query("""
        select o from PaymentCancelOutBox o
        where o.status in ('PENDING')
          and o.nextRunAt <= :now
        order by o.nextRunAt asc
        """)
    List<PaymentCancelOutBox> findRunnable(@Param("now") LocalDateTime now, Pageable pageable);


    @Modifying
    @Transactional
    @Query("""
      update PaymentCancelOutBox o
         set o.status='CLAIMED', o.lockedBy=:worker, o.lockedAt=:now, o.updatedAt=:now
       where o.id=:id
         and o.status='PENDING'
         and o.lockedBy is null
    """)
    int claim(@Param("id") Long id, @Param("worker") String workerId, @Param("now") LocalDateTime now);

}
