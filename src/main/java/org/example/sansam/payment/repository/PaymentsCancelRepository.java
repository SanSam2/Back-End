package org.example.sansam.payment.repository;

import org.example.sansam.payment.domain.PaymentCancellation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentsCancelRepository extends JpaRepository<PaymentCancellation, Long> {
}
