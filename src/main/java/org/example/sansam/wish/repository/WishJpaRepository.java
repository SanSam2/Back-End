package org.example.sansam.wish.repository;

import org.example.sansam.wish.domain.Wish;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WishJpaRepository extends JpaRepository<Wish, Long> {
    Optional<Wish> findByUserIdAndProductId(Long userId, Long productId);
}
