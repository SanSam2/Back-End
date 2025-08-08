package org.example.sansam.order.tmp;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TmpProductRepository extends JpaRepository<TmpProducts, Long> {
    Optional<TmpProducts> findById(Long id);
}
