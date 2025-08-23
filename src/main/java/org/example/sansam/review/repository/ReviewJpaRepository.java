package org.example.sansam.review.repository;

import org.example.sansam.review.domain.Review;
import org.example.sansam.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewJpaRepository extends JpaRepository<Review, Long> {
    List<Review> findAllByProductId(Long productId);

    List<Review> findAllByUserId(Long userId);

    Review findByProductIdAndUserId(Long productId, Long userId);

    boolean existsByUser(User user);

    boolean existsByUser_Id(Long userId);

    //    boolean existsByUserIdAndProductId(Long userId, Long productId);

//    @Query("SELECT r.product.id FROM Review r " +
//            "WHERE r.user.id = :userId AND r.product.id IN :productIds")
//    List<Long> findReviewedProductIdsByUserAndProducts(
//            @Param("userId") Long userId,
//            @Param("productIds") List<Long> productIds
//    );

//    boolean existsByUserIdAndOrderId(Long productId, Long userId);
}
