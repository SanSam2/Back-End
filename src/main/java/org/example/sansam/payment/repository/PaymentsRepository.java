package org.example.sansam.payment.repository;

import org.example.sansam.payment.domain.Payments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentsRepository extends JpaRepository<Payments, Long> {
}
