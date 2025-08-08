package org.example.sansam.notification.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.sansam.notification.event.*;
import org.example.sansam.product.domain.ProductDetail;
import org.example.sansam.product.repository.ProductDetailJpaRepository;
import org.example.sansam.product.repository.ProductJpaRepository;
import org.example.sansam.user.domain.User;
import org.example.sansam.user.repository.UserRepository;
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
