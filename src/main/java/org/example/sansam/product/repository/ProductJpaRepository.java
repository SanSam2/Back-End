package org.example.sansam.product.repository;

import org.example.sansam.product.domain.Category;
import org.example.sansam.product.domain.Product;
import org.example.sansam.search.dto.SearchListResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductJpaRepository extends JpaRepository<Product, Long> {
    @Query("SELECT COUNT(r) FROM Review r WHERE r.product.id = :productId")
    Long countReviewsByProductId(@Param("productId") Long productId);

    @Query("SELECT p FROM Product p " +
            "WHERE (:category IS NULL OR p.category.smallName = :category) " +
            "AND (:keyword IS NULL OR " +
            "LOWER(p.productName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.category.smallName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Product> findByCategoryOrKeyword(
            @Param("keyword") String keyword,
            @Param("category") String category,
            Pageable pageable
    );

    @Query("SELECT p FROM Product p left JOIN p.wishList w GROUP BY p.id ORDER BY COUNT(w) DESC")
    List<Product> findTopWishListProduct();

    @Query(value = "select p from Products p Order By p.viewCount desc limit 10", nativeQuery = true)
    List<Product> findProductsOrderByViewCountDesc();

    List<Product> findByCategoryOrderByViewCountDesc(Category category);
}
