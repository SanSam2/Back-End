package org.example.sansam.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.sansam.cart.dto.CartDTO;
import org.example.sansam.chat.dto.ChatDTO;
import org.example.sansam.notification.domain.Notification;
import org.example.sansam.notification.domain.NotificationHistory;
import org.example.sansam.notification.dto.*;
import org.example.sansam.notification.repository.NotificationHistoryRepository;
import org.example.sansam.notification.repository.NotificationsRepository;
import org.example.sansam.payment.dto.PaymentDTO;
import org.example.sansam.product.domain.Product;
import org.example.sansam.product.repository.ProductRepository;
import org.example.sansam.review.dto.ReviewDTO;
import org.example.sansam.user.dto.UserDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Log4j2
public class NotificationService {
    private final Map<Long, SseEmitter> sseEmitters = new ConcurrentHashMap<>();
    private final NotificationsRepository notificationRepository;
    private final NotificationHistoryRepository notificationHistoryRepository;
    private final ProductRepository productRepository;
    private static final Long DEFAULT_TIMEOUT = 60 * 60 * 1000L; // 기본 60분 connect 설정
    private final Timestamp now = Timestamp.valueOf(LocalDateTime.now());

    // 사용자 로그인 후 sse 연결
    public SseEmitter connect(Long userId) {

        SseEmitter sseEmitter = new SseEmitter(DEFAULT_TIMEOUT);
        sseEmitters.put(userId, sseEmitter);

        try { // 연결 성공 시, 초기 이벤트 전송
            sseEmitter.send(SseEmitter.event()
                    .name("connect")
                    .data("SSE 연결 완료"));

        } catch (IOException io) { // 전송 실패 시 emitter 제거 및 예외 처리
            log.error("sse 연결 중 오류 발생 (userId: {})", userId, io);
            sseEmitter.completeWithError(io);
            sseEmitters.remove(userId);
        }

        sseEmitter.onCompletion(() -> sseEmitters.remove(userId)); // Emitter가 완료될 때(모든 데이터가 성공적으로 전송된 상태) Emitter 삭제
        sseEmitter.onTimeout(() -> sseEmitters.remove(userId)); // TIMEOUT 됐을 때 삭제
        sseEmitter.onError((error) -> sseEmitters.remove(userId)); // 에러 발생 시 삭제

        return sseEmitter;
    }

    //     각 알림이 필요한 시점에서 사용하는 알림 메시지 분기 메서드
    public void sendDynamicNotification(Long userId, Object payload) {
        if (payload instanceof UserNotiDTO) {
            sendWelcomeNotification(userId, (UserNotiDTO) payload);
        } else if (payload instanceof PaymentNotiDTO) {
            sendPaymentCompleteNotification(userId, (PaymentNotiDTO) payload);
        } else if (payload instanceof PaymentCancelNotiDTO) {
            sendPaymentCancelNotification(userId, (PaymentCancelNotiDTO) payload);
        } else if (payload instanceof CartNotiDTO) {
            sendCartLowNotification(userId, (CartNotiDTO) payload);
        } else if (payload instanceof WishListNotiDTO) {
            sendWishListLowNotification(userId, (WishListNotiDTO) payload);
        } else if (payload instanceof ReviewNotiDTO) {
            sendReviewRequestNotification(userId, (ReviewNotiDTO) payload);
        } else if (payload instanceof ChatNotiDTO) {
            sendChatNotification(userId, (ChatNotiDTO) payload);
        }
    }

    // user 정보 받아와서 회원가입 환영 축하 알림 생성
    public void sendWelcomeNotification(Long userId, UserNotiDTO data) {
        if (data != null) {
            try {
                Optional<Notification> template = notificationRepository.findByNotificationId(1L);

                String formattedMessage = template.map(Notification::getMessage)
                        .map(msg -> String.format(msg, data.getUsername()))
                        .orElse("회원가입 환영 메시지 템플릿을 가져올 수 없습니다.");

                NotificationHistory dto = NotificationHistory.builder()
                        .user_id(userId)
                        .notification_id(1L)
                        .createdAt(now)
                        .expiredAt(Timestamp.valueOf(LocalDateTime.now().plusDays(14)))
                        .message(formattedMessage)
                        .build();

                sseEmitters.get(userId).send(SseEmitter.event()
                        .name("welcome message")
                        .data(dto));

                notificationHistoryRepository.save(dto);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // user 정보 받아와서 결제 완료 알림 생성
    public void sendPaymentCompleteNotification(Long userId, PaymentNotiDTO data) {
        if (data != null) {
            try {
                Optional<Notification> template = notificationRepository.findByNotificationId(2L);
//                Optional<Product> product = productRepository.
                String formattedMessage = template.map(Notification::getMessage)
                        .map(msg -> String.format(msg, data.getFinalPrice()))
                        .orElse("결제 완료 메시지 템플릿을 가져올 수 없습니다.");

                NotificationHistory dto = NotificationHistory.builder()
                        .user_id(userId)
                        .notification_id(2L)
                        .createdAt(now)
                        .expiredAt(Timestamp.valueOf(LocalDateTime.now().plusDays(14)))
                        .message(formattedMessage)
                        .build();

                sseEmitters.get(userId).send(SseEmitter.event()
                        .name("Payment Complete Message")
                        .data(dto));

                notificationHistoryRepository.save(dto);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // user 정보 받아와서 결제 취소 완료 알림 생성
    public void sendPaymentCancelNotification(Long userId, PaymentCancelNotiDTO data) {

        if (data != null) {
            try {
                Optional<Notification> template = notificationRepository.findByNotificationId(3L);

                String formattedMessage = template.map(Notification::getMessage)
                        .map(msg -> String.format(msg, data.getRefundPrice()))
                        .orElse("결제 취소 완료 메시지 템플릿을 가져올 수 없습니다.");

                NotificationHistory dto = NotificationHistory.builder()
                        .user_id(userId)
                        .notification_id(3L)
                        .createdAt(now)
                        .expiredAt(Timestamp.valueOf(LocalDateTime.now().plusDays(14)))
                        .message(formattedMessage)
                        .build();

                sseEmitters.get(userId).send(SseEmitter.event()
                        .name("Payment Cancel Complete Message")
                        .data(dto));

                notificationHistoryRepository.save(dto);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    // user 정보 받아와서 장바구니 상품 품절 임박 완료 알림 생성
    public void sendCartLowNotification(Long userId, CartNotiDTO data) {

        if (data != null) {
            try {
                Optional<Notification> template = notificationRepository.findByNotificationId(4L);

                String formattedMessage = template.map(Notification::getMessage)
                        .map(msg -> String.format(msg, data.getProductName()))
                        .orElse("장바구니 상품 품절 임박 메시지 템플릿을 가져올 수 없습니다.");

                NotificationHistory dto = NotificationHistory.builder()
                        .user_id(userId)
                        .notification_id(4L)
                        .createdAt(now)
                        .expiredAt(Timestamp.valueOf(LocalDateTime.now().plusDays(14)))
                        .message(formattedMessage)
                        .build();

                sseEmitters.get(userId).send(SseEmitter.event()
                        .name("Cart Product Stock Low Message")
                        .data(dto));

                notificationHistoryRepository.save(dto);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    // user 정보 받아와서 위시리스트 상품 품절 임박 완료 알림 생성
    public void sendWishListLowNotification(Long userId, WishListNotiDTO data) {

        if (data != null) {
            try {
                Optional<Notification> template = notificationRepository.findByNotificationId(5L);

                String formattedMessage = template.map(Notification::getMessage)
                        .map(msg -> String.format(msg, data.getProductName()))
                        .orElse("위시리스트 상품 품절 임박 메시지 템플릿을 가져올 수 없습니다.");

                NotificationHistory dto = NotificationHistory.builder()
                        .user_id(userId)
                        .notification_id(5L)
                        .createdAt(now)
                        .expiredAt(Timestamp.valueOf(LocalDateTime.now().plusDays(14)))
                        .message(formattedMessage)
                        .build();

                sseEmitters.get(userId).send(SseEmitter.event()
                        .name("WishList Product Stock Low Message")
                        .data(dto));

                notificationHistoryRepository.save(dto);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    // user 정보 받아와서 리뷰 요청 완료 알림 생성
    public void sendReviewRequestNotification(Long userId, ReviewNotiDTO data) {

        if (data != null) {
            try {
                Optional<Notification> template = notificationRepository.findByNotificationId(6L);

                String formattedMessage = template.map(Notification::getMessage)
                        .map(msg -> String.format(msg, data.getProductName()))
                        .orElse("리뷰 요청 메시지 템플릿을 가져올 수 없습니다.");

                NotificationHistory dto = NotificationHistory.builder()
                        .user_id(userId)
                        .notification_id(6L)
                        .createdAt(now)
                        .expiredAt(Timestamp.valueOf(LocalDateTime.now().plusDays(14)))
                        .message(formattedMessage)
                        .build();

                sseEmitters.get(userId).send(SseEmitter.event()
                        .name("Review Request Message")
                        .data(dto));

                notificationHistoryRepository.save(dto);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void sendChatNotification(Long userId, ChatNotiDTO data) {

        if (data != null) {
            try {
                Optional<Notification> template = notificationRepository.findByNotificationId(7L);

                String formattedMessage = template.map(Notification::getMessage)
                        .map(msg -> String.format(msg, data.getMessage()))
                        .orElse("채팅 알림 템플릿을 가져올 수 없습니다.");

                NotificationHistory dto = NotificationHistory.builder()
                        .user_id(userId)
                        .notification_id(6L)
                        .createdAt(now)
                        .expiredAt(Timestamp.valueOf(LocalDateTime.now().plusDays(14)))
                        .message(formattedMessage)
                        .build();

                sseEmitters.get(userId).send(SseEmitter.event()
                        .name("Chat Notification Message")
                        .data(dto));

                notificationHistoryRepository.save(dto);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
