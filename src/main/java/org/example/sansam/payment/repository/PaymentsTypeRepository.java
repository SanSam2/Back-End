package org.example.sansam.payment.repository;

import org.example.sansam.payment.domain.PaymentMethodType;
import org.example.sansam.payment.domain.PaymentsType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentsTypeRepository extends JpaRepository<PaymentsType,Long> {

    Optional<PaymentsType> findByTypeName(PaymentMethodType typeName);
}
