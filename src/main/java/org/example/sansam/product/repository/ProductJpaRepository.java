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

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProductJpaRepository extends JpaRepository<Product, Long> {
    @Query("SELECT COUNT(r) FROM Review r WHERE r.product.id = :productId")
    Long countReviewsByProductId(@Param("productId") Long productId);

    @Query("""
    SELECT p FROM Product p
    WHERE
        (:keyword IS NULL OR
            LOWER(p.productName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
            LOWER(p.category.smallName) LIKE LOWER(CONCAT('%', :keyword, '%')))
""")
    Page<Product> findByKeyword(
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @Query("""
    SELECT p FROM Product p
    WHERE
       (:bigCategory IS NULL OR p.category.bigName = :bigCategory)
    AND (:middleCategory IS NULL OR p.category.middleName = :middleCategory)
    AND (:smallCategory IS NULL OR p.category.smallName = :smallCategory)
""")
    Page<Product> findByCategoryNames(
            @Param("bigCategory") String bigCategory,
            @Param("middleCategory") String middleCategory,
            @Param("smallCategory") String smallCategory,
            Pageable pageable
    );


    @Query("SELECT p FROM Product p left JOIN p.wishList w GROUP BY p.id ORDER BY COUNT(w) DESC LIMIT 10")
    List<Product> findTopWishListProduct();

    @Query("SELECT p FROM Product p ORDER BY p.viewCount DESC LIMIT 10")
    List<Product> findProductsOrderByViewCountDesc();

    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId ORDER BY p.viewCount DESC LIMIT 10")
    List<Product> findByCategoryIdOrderByViewCountDesc(@Param("categoryId") Long categoryId);

    @Query("SELECT p FROM Product p WHERE p.createdAt > :createdAt")
    List<Product> findAfterCreatedAt(LocalDateTime createdAt);
}
