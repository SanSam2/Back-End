package org.example.sansam.notification.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.sansam.chat.repository.ChatMemberRepository;
import org.example.sansam.chat.repository.ChatRoomRepository;
import org.example.sansam.notification.event.email.PaymentCanceledEmailEvent;
import org.example.sansam.notification.event.email.PaymentCompleteEmailEvent;
import org.example.sansam.notification.event.sse.PaymentCancelEvent;
import org.example.sansam.notification.event.sse.PaymentCompleteEvent;
import org.example.sansam.notification.event.sse.ProductQuantityLowEvent;
import org.example.sansam.product.domain.ProductDetail;
import org.example.sansam.product.repository.ProductDetailJpaRepository;
import org.example.sansam.product.repository.ProductJpaRepository;
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
    private final ChatMemberRepository chatMemberRepository;
    private final ChatRoomRepository chatRoomRepository;

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


//            String orderName = "호상상 외 2건";
//            String productName = "유노상반팔";
//            String senderName = "김김김";
//            String message = "집에 가고 싶구나..";


            ProductDetail productDetail = productDetailJpaRepository.findById(1L).orElseThrow();

            User user = userRepository.findById(33L).orElseThrow();
            String orderName = "무신사 반팔";
            Long orderPrice = 10000L;

//            publisher.publishEvent(new PaymentCompleteEvent(user, orderName, orderPrice));
//            publisher.publishEvent(new PaymentCompleteEmailEvent(user, orderName, orderPrice));

            publisher.publishEvent(new PaymentCancelEvent(user, orderName, orderPrice));
            publisher.publishEvent(new PaymentCanceledEmailEvent(user, orderName, orderPrice));

//            ChatRoom chatRoom = chatRoomRepository.findById(1L).orElseThrow();
//            User sender = userRepository.findById(1L).orElseThrow();
//            log.info("chatRoom - {}", chatRoom);
//            publisher.publishEvent(new ChatEvent(chatRoom, sender,"이게 바로 테스트 성공"));

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }


}
