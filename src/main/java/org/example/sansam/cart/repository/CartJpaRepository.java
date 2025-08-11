package org.example.sansam.cart.repository;

import org.example.sansam.cart.domain.Cart;
import org.example.sansam.product.domain.ProductDetail;
import org.example.sansam.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartJpaRepository extends JpaRepository<Cart, Long> {

    @Query("SELECT c FROM Cart c where c.user = :user")
    Page<Cart> findAllByUser(User user, Pageable pageable);

    @Query("SELECT c FROM Cart c WHERE c.user = :user and c.productDetail = :productDetail")
    Cart findByUserIdAndProductDetail(User user, ProductDetail productDetail);

    @Query("select c.user from Cart c where c.productDetail.id =:productDetailId")
    List<User> findUsersByProductDetail_Id(Long productDetailId);
}