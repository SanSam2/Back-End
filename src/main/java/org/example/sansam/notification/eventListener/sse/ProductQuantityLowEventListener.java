package org.example.sansam.notification.eventListener.sse;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.sansam.cart.repository.CartJpaRepository;
import org.example.sansam.notification.event.sse.ProductQuantityLowEvent;
import org.example.sansam.notification.service.NotificationService;
import org.example.sansam.product.domain.Product;
import org.example.sansam.product.domain.ProductDetail;
import org.example.sansam.user.domain.User;
import org.example.sansam.wish.repository.WishJpaRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
@Log4j2
public class ProductQuantityLowEventListener {
    private final NotificationService notificationService;
    private final CartJpaRepository cartJpaRepository;
    private final WishJpaRepository wishJpaRepository;

    /**
     * Handles low product quantity events by notifying users with the affected product in their cart or wishlist.
     * <p>
     * When a product's quantity becomes low, this method identifies users who have the product detail in their cart or the product in their wishlist and sends them appropriate low stock notifications.
     *
     * @param event the event containing information about the product detail with low quantity
     */
    @EventListener
    public void handleProductQuantityLowEvent(ProductQuantityLowEvent event) {
        ProductDetail productDetail = event.getProductDetail(); // 상품 디테일
        Product product = productDetail.getProduct();   // 그 디테일을 가진 상품

        Long detailId = productDetail.getId();
        Long productId = product.getId();
        String productName = product.getProductName();

        // 장바구니 유저 찾고 각각 유저에게 상품 이름 갖고 알림 전송
        List<User> cartUsers = cartJpaRepository.findUsersByProductDetail_Id(detailId);
        cartUsers.forEach(user -> {
            try {
                notificationService.sendCartLowNotification(user, productName);
            } catch (Exception e) {
                log.error("장바구니 저재고 알림 실패 - userId={}, product={}", user.getId(), productName, e);
            }
        });

        // 위시리스트 유저 찾고 각각 유저에게 상품 이름 갖고 알림 전송
        List<User> wishUsers = wishJpaRepository.findUsersByProduct_Id(productId);
        wishUsers.forEach(user -> {
            try {
                notificationService.sendWishListLowNotification(user, productName);
            } catch (Exception e) {
                log.error("위시리스트 저재고 알림 실패 - userId={}, product={}", user.getId(), productName, e);
            }
        });

    }
}
