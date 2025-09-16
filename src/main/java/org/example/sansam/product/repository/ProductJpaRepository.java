package org.example.sansam.product.repository;

import org.example.sansam.product.domain.Category;
import org.example.sansam.product.domain.Product;
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
    @Query(value = """
        SELECT *
        FROM products
        WHERE MATCH(brand_name) AGAINST(:brand IN BOOLEAN MODE)
        ORDER BY view_count DESC
        LIMIT :size OFFSET :offset
    """, nativeQuery = true)
    List<Product> findByBrandNameWithPaging(
            @Param("brand") String brand,
            @Param("size") int size,
            @Param("offset") int offset
    );

    @Query("SELECT COUNT(r) FROM Review r WHERE r.product.id = :productId")
    Long countReviewsByProductId(@Param("productId") Long productId);

    @Query("""
        SELECT p FROM Product p
        WHERE
            (:keyword IS NULL OR
                LOWER(p.productName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                LOWER(p.category.smallName) LIKE LOWER(CONCAT('%', :keyword, '%'))OR
                LOWER(p.category.bigName) LIKE LOWER(CONCAT('%', :keyword, '%'))OR
                LOWER(p.category.middleName) LIKE LOWER(CONCAT('%', :keyword, '%'))OR
                LOWER(p.brandName) LIKE LOWER(CONCAT('%', :keyword, '%')))
    """)
    Page<Product> findByKeywordV1(
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
    Page<Product> findByCategoryNamesV1(
            @Param("bigCategory") String bigCategory,
            @Param("middleCategory") String middleCategory,
            @Param("smallCategory") String smallCategory,
            Pageable pageable
    );

    //--------------v2--------------
    //UNION SELECT p from Product p where p.category.id in categories


//    Page<Product> findByKeywordWithCategoryV2(
//            @Param("keyword") String keyword,
//            Pageable pageable
//    );


//    @Query(value = """
//    SELECT p.*,
//           MATCH(p.product_name, p.brand_name) AGAINST(:keyword IN NATURAL LANGUAGE MODE) AS score
//    FROM products p
//    WHERE MATCH(p.product_name, p.brand_name) AGAINST(:keyword IN NATURAL LANGUAGE MODE) > 2.5
//    ORDER BY score DESC
//    """,
//            countQuery = """
//    SELECT COUNT(*)
//    FROM products p
//    WHERE MATCH(p.product_name, p.brand_name) AGAINST(:keyword IN NATURAL LANGUAGE MODE) > 2.5
//    """,
//            nativeQuery = true)
//    Page<Product> findByKeywordV2(
//            @Param("keyword") String keyword,
//            Pageable pageable
//    );

    @Query(value = """
        SELECT p
        FROM Product p
    """)
    Page<Product> findByNullKeywordV2(
            Pageable pageable
    );

    @Query(value = """
        SELECT p FROM Product p
        WHERE p.category.id = :categoryId
    """)
    Page<Product> findByCategoryNamesV2(
            @Param("categoryId") Long categoryId,
            Pageable pageable
    );

    @Query(value = """
        SELECT p FROM Product p
        WHERE p.category.id in :categoryIds
        ORDER BY p.viewCount LIMIT 1000
    """)
    List<Product> findByCategoryListV2(
            @Param("categoryIds") List<Long> categoryIds
    );

    @Query(value = """
        SELECT *
        FROM products
        WHERE  MATCH(product_name, brand_name) AGAINST(:keyword IN BOOLEAN MODE)
        LIMIT 1000
    """, nativeQuery = true)
    List<Product> findByKeywordV2(
            @Param("keyword") String keyword
    );


    @Query("SELECT p FROM Product p left JOIN p.wishList w GROUP BY p.id ORDER BY COUNT(w) DESC LIMIT 40")
    List<Product> findTopWishListProduct();

    @Query("SELECT p FROM Product p ORDER BY p.viewCount DESC LIMIT 10")
    List<Product> findProductsOrderByViewCountDesc();

    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId ORDER BY p.viewCount DESC LIMIT 40")
    List<Product> findByCategoryIdOrderByViewCountDesc(@Param("categoryId") Long categoryId);

    @Query("SELECT p FROM Product p WHERE p.createdAt > :createdAt")
    List<Product> findAfterCreatedAt(LocalDateTime createdAt);

    List<Product> findByCategoryOrderByViewCountDesc(Category category);

    @Query(value = """
        SELECT product_id
        FROM products
        WHERE MATCH(product_name, brand_name)  AGAINST(:keyword IN NATURAL LANGUAGE MODE) > 2.7
        LIMIT 1000
    """, nativeQuery = true)
    List<Long> findIdsByKeyword(@Param("keyword") String keyword);

    @Query("SELECT p.id FROM Product p WHERE p.category.id IN :categories")
    List<Long> findIdsByCategories(@Param("categories") List<Long> categories);

    @Query("SELECT p FROM Product p WHERE p.id IN :ids")
    List<Product> findAllByIds(@Param("ids") List<Long> ids, Pageable pageable);

    // Repository
    @Query(value = """
    SELECT p.product_id
    FROM products p
    WHERE MATCH(p.product_name, p.brand_name) AGAINST(:booleanKeyword IN BOOLEAN MODE)
    LIMIT :limit
    """, nativeQuery = true)
    List<Long> findCandidateIdsWithoutCategory(@Param("booleanKeyword") String booleanKeyword,
                                               @Param("limit") int limit);

    @Query(value = """
    SELECT p.product_id
    FROM products p
    WHERE MATCH(p.product_name, p.brand_name) AGAINST(:booleanKeyword IN BOOLEAN MODE)
      AND p.category_id IN (:categories)
    LIMIT :limit
    """, nativeQuery = true)
    List<Long> findCandidateIdsWithCategory(@Param("booleanKeyword") String booleanKeyword,
                                            @Param("categories") List<Long> categories,
                                            @Param("limit") int limit);


    // NATURAL MODE 점수 정렬 (카테고리 포함)
    @Query(value = """
        SELECT p.product_id,
               MATCH(p.product_name, p.brand_name) AGAINST(:keyword IN NATURAL LANGUAGE MODE) AS score
        FROM products p
        WHERE p.product_id IN (:candidateIds)
          AND (:categories IS NULL OR p.category_id IN (:categories))
        ORDER BY score DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> rankCandidatesWithCategory(@Param("keyword") String keyword,
                                              @Param("candidateIds") List<Long> candidateIds,
                                              @Param("categories") List<Long> categories,
                                              @Param("limit") int limit);

}

