package org.example.sansam.payment.repository;

import org.example.sansam.payment.domain.Payments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentsRepository extends JpaRepository<Payments, Long> {
    Optional<Payments> findByPaymentKey(String paymentKey);
}
