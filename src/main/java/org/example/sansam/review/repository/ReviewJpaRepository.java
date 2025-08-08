package org.example.sansam.review.repository;

import org.example.sansam.review.domain.Review;
import org.example.sansam.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewJpaRepository extends JpaRepository<Review, Long> {
    List<Review> findAllByProductId(Long productId);

    List<Review> findAllByUserId(Long userId);

    Review findByProductIdAndUserId(Long productId, Long userId);

    boolean existsByUser(User user);

    boolean existsByUser_Id(Long userId);

    boolean existsByUserIdAndProductId(Long userId, Long productId);

//    boolean existsByUserIdAndOrderId(Long productId, Long userId);
}
