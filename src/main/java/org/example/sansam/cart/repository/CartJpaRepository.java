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

    /**
     * Retrieves the cart entry for the specified user and product detail.
     *
     * @param user the user whose cart is being queried
     * @param productDetail the product detail to match in the cart
     * @return the cart entry matching the user and product detail, or null if none exists
     */
    @Query("SELECT c FROM Cart c WHERE c.user = :user and c.productDetail = :productDetail")
    Cart findByUserIdAndProductDetail(User user, ProductDetail productDetail);

    /**
     * Retrieves a list of users who have carts containing the specified product detail.
     *
     * @param productDetailId the ID of the product detail to search for in carts
     * @return a list of users associated with carts that include the given product detail
     */
    @Query("select c.user from Cart c where c.productDetail.id =:productDetailId")
    List<User> findUsersByProductDetail_Id(Long productDetailId);
}