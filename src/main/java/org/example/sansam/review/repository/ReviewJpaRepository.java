package org.example.sansam.review.repository;

import org.example.sansam.review.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewJpaRepository extends JpaRepository<Review, Long> {
    List<Review> findAllByProduct_ProductId(Long productId);

//    List<Review> findByUserUserId(Long userId);

    Review findByProduct_ProductIdAndUser_UserId(Long productId, Long userId);
}
