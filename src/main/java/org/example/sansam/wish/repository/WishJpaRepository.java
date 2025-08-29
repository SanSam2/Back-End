package org.example.sansam.wish.repository;

import lombok.Getter;
import org.example.sansam.user.domain.User;
import org.example.sansam.wish.domain.Wish;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishJpaRepository extends JpaRepository<Wish, Long> {
    Optional<Wish> findByUserIdAndProductId(Long userId, Long productId);

    @Query(value = "select * from wishes w where w.user_id = :userId order by w.created_at desc limit 1", nativeQuery = true)
    Wish findTopByUserIdOrderByCreated_atDesc(Long userId);

    List<Wish> findByUserIdAndProductIdIn(Long userId, List<Long> productIds);

    @EntityGraph(attributePaths = {"product", "product.fileManagement"})
    @Query("SELECT w FROM Wish w WHERE w.user.id = :userId ORDER BY w.createdAt DESC")
    Page<Wish> findWishesByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query(value = "select w.user from Wish w where w.product.id = :productId")
    List<User> findUsersByProduct_Id(Long productId);

    @Query("select Count(*) from Wish w where w.product.id = :productId")
    Long countByProductId(Long productId);
}
