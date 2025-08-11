package org.example.sansam.product.repository;

import org.example.sansam.product.domain.Product;
import org.example.sansam.product.domain.ProductDetail;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductDetailJpaRepository extends JpaRepository<ProductDetail,Long> {
    List<ProductDetail> findByProduct(Product product);

    @Query("""
    select distinct pd from ProductDetail pd
    left join fetch pd.productConnects pc
    left join fetch pc.option
    where pd.product = :product
""")
    List<ProductDetail> findByProductWithConnects(@Param("product") Product product);

    @Query("""
        select pd
          from ProductDetail pd
         where pd.product.id = :productId
           and exists (
               select 1 from ProductConnect pc
               join pc.option o
               where pc.productDetail = pd
                 and o.type = :sizeType and o.name = :size
           )
           and exists (
               select 1 from ProductConnect pc2
               join pc2.option o2
               where pc2.productDetail = pd
                 and o2.type = :colorType and o2.name = :color
           )
    """)
    Optional<ProductDetail> findDetailByProductAndSizeColor(
            @Param("productId") Long productId,
            @Param("sizeType") String sizeType,
            @Param("size") String size,
            @Param("colorType") String colorType,
            @Param("color") String color
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update ProductDetail pd
           set pd.quantity = pd.quantity - :num,
               pd.version  = pd.version + 1
         where pd.id      = :id
           and pd.version = :version
           and pd.quantity >= :num
    """)
    int tryDecrement(@Param("id") Long id,
                     @Param("num") long num,
                     @Param("version") Long version);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
    update ProductDetail pd
       set pd.quantity = pd.quantity + :num,
           pd.version  = pd.version + 1
     where pd.id      = :id
       and pd.version = :version
    """)
    int tryIncrement(@Param("id") Long id,
                     @Param("num") long num,
                     @Param("version") Long version);
}
