package org.example.sansam.notification.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.sansam.notification.event.*;
import org.example.sansam.notification.service.NotificationService;
import org.example.sansam.order.tmp.ProductRepository;
import org.example.sansam.product.domain.Product;
import org.example.sansam.product.domain.ProductDetail;
import org.example.sansam.product.repository.ProductDetailJpaRepository;
import org.example.sansam.product.repository.ProductJpaRepository;
import org.example.sansam.user.domain.User;
import org.example.sansam.user.repository.UserRepository;
import org.example.sansam.user.service.UserService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/test")
public class NotificationTestController {

    private final UserRepository userRepository;
    private final ApplicationEventPublisher publisher;
    private final ProductDetailJpaRepository productDetailJpaRepository;
    private final ProductJpaRepository productJpaRepository;

    /**
     * Handles a test notification by simulating a product order and conditionally publishing a low quantity event.
     *
     * Retrieves a user and a product detail, simulates an order by reducing the product quantity, and publishes a
     * {@code ProductQuantityLowEvent} if the product's quantity crosses from above 50 to 50 or below. Returns an HTTP 200 OK
     * response on success, or an HTTP 500 response with the error message if an exception occurs.
     *
     * @return HTTP 200 OK if the operation succeeds; HTTP 500 with an error message if an exception is thrown.
     */
    @PostMapping("/send")
    public ResponseEntity<?> sendTestNotification() {
        try {
            String email = "qwe123@naver.com";
            User user = userRepository.findByEmail(email).orElseThrow();
            log.info("user: {}", user.getName());
//            String orderName = "호상상 외 2건";
//            String productName = "유노상반팔";
//            String senderName = "김김김";
//            String message = "집에 가고 싶구나..";

            ProductDetail productDetail = productDetailJpaRepository.findById(1L).orElseThrow();

            Long beforeQuantity = productDetail.getQuantity();
            log.info("beforeQuantity: {}", beforeQuantity);
            Long orderQuantity = 5L;
            Long afterQuantity = beforeQuantity - orderQuantity;
            log.info("afterQuantity: {}", afterQuantity);

            if (beforeQuantity > 50L && afterQuantity <= 50L) {
                publisher.publishEvent(new ProductQuantityLowEvent(productDetail));
            }

            return ResponseEntity.ok().build();
        }catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }


}
