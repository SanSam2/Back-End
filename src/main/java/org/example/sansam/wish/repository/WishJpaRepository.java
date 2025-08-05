package org.example.sansam.wish.repository;

import lombok.Getter;
import org.example.sansam.user.domain.User;
import org.example.sansam.wish.domain.Wish;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WishJpaRepository extends JpaRepository<Wish, Long> {
    Optional<Wish> findByUserIdAndProductId(Long userId, Long productId);

    @Query(value = "select * from wishes w where w.user_id = :userId order by w.created_at desc limit 10", nativeQuery = true)
    Wish findTopByUserIdOrderByCreated_atDesc(Long userId);
}
