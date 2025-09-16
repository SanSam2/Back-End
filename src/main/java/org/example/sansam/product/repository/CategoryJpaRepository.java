package org.example.sansam.product.repository;

import org.example.sansam.product.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryJpaRepository extends JpaRepository<Category, Long> {
//    @Query(value = """
//    SELECT c.category_id FROM categories c
//    WHERE c.big_name LIKE CONCAT('%', :name, '%')
//       OR c.middle_name LIKE CONCAT('%', :name, '%')
//       OR c.small_name LIKE CONCAT('%', :name, '%')
//""", nativeQuery = true)
    @Query("""
    SELECT c.id FROM Category c
    WHERE c.bigName LIKE concat('%', :cate, '%') or
          c.middleName LIKE concat('%', :cate, '%') or
        c.smallName LIKE concat('%', :cate, '%')
""")
    List<Long> findCategoryName(@Param("cate") String cate);

    @Query("""
    SELECT c FROM Category c
    WHERE c.bigName = :big
       AND c.middleName = :middle
       AND c.smallName = :small
""")
    Category findCategoryByBigNameAndMiddleNameAndSmallName(String big, String middle, String small);
}

