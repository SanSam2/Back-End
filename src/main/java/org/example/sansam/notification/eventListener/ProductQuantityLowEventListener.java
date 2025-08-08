package org.example.sansam.notification.eventListener;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.example.sansam.cart.repository.CartJpaRepository;
import org.example.sansam.cart.repository.CartRepository;
import org.example.sansam.notification.event.ProductQuantityLowEvent;
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
public class ProductQuantityLowEventListener {
    private final NotificationService notificationService;
    private final CartJpaRepository cartJpaRepository;
    private final WishJpaRepository wishJpaRepository;

    @EventListener
    public void handleProductQuantityLowEvent(ProductQuantityLowEvent event) {
        ProductDetail productDetail = event.getProductDetail(); // 상품 디테일
        Product product = productDetail.getProduct();   // 그 디테일을 가진 상품

        Long detailId = productDetail.getId();
        Long productId = product.getId();
        String productName = product.getProductName();

        // 장바구니 유저 찾고 각각 유저에게 상품 이름 갖고 알림 전송
        List<User> cartUsers = cartJpaRepository.findUsersByProductDetail_Id(detailId);
        cartUsers.forEach(user -> notificationService.sendCartLowNotification(user, productName));

        // 위시리스트 유저 찾고 각각 유저에게 상품 이름 갖고 알림 전송
        List<User> wishUsers = wishJpaRepository.findUsersByProduct_Id(productId);
        wishUsers.forEach(user -> notificationService.sendWishListLowNotification(user, productName));

    }
}
