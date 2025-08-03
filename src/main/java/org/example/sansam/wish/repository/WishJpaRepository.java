package org.example.sansam.wish.repository;

import org.example.sansam.wish.domain.Wish;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WishJpaRepository extends JpaRepository<Wish, Long> {
    Wish findByUserIdAndProductId(Long userId, Long productId);
}
