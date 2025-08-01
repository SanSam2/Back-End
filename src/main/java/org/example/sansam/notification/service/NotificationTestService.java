package org.example.sansam.notification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.sansam.notification.domain.Notification;
import org.example.sansam.notification.domain.NotificationHistories;
import org.example.sansam.notification.dto.NotificationDTO;
import org.example.sansam.notification.exception.NotificationException;
import org.example.sansam.notification.repository.NotificationHistoriesRepository;
import org.example.sansam.notification.repository.NotificationsRepository;
import org.example.sansam.user.domain.User;
import org.example.sansam.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationTestService {

    private final NotificationsRepository notificationRepository;
    private final NotificationHistoriesRepository notificationHistoriesRepository;
    private final UserRepository userRepository;
    private final Map<Long, SseEmitter> sseEmitters = new ConcurrentHashMap<>();
    @Autowired
    private ObjectMapper objectMapper;

    private static final long DEFAULT_TIMEOUT = 60L * 1000 * 60;

    // === Public Methods (외부 호출용 API) ===

    public SseEmitter connect(Long userId) {
        try {
            SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
            sseEmitters.put(userId, emitter);

            emitter.onCompletion(() -> {
                sseEmitters.remove(userId);
                log.info("SSE 연결 완료. 사용자 ID: {}", userId);
            });

            emitter.onTimeout(() -> {
                sseEmitters.remove(userId);
                log.warn("SSE 연결 타임아웃. 사용자 ID: {}", userId);
            });

            emitter.onError(ex -> {
                sseEmitters.remove(userId);
                log.error("SSE 연결 에러. 사용자 ID: {}, 에러: {}", userId, ex.getMessage());
            });

            return emitter;
        } catch (Exception e) {
            log.error("SSE 연결 생성 실패. 사용자 ID: {}", userId, e);
            throw new NotificationException("알림 연결 설정 실패", e);
        }

    }
    // Test
    public void sendWelcomeTestNotification(Long userId, String name) {

        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(()-> new RuntimeException("회원이 없습니다."));

            Notification template = notificationRepository.findById(1L)
                    .orElseThrow(() -> new IllegalArgumentException("환영 알림 템플릿이 없습니다"));

            NotificationHistories notification = NotificationHistories.builder()
                    .user(user)
                    .notification(template)
                    .title(String.format(template.getTitle(), user.getName()))
                    .message(template.getMessage())
                    .createdAt(Timestamp.valueOf(LocalDateTime.now()))
                    .expiredAt(Timestamp.valueOf(LocalDateTime.now().plusDays(14)))
                    .build();

            NotificationDTO dto = NotificationDTO.builder()
                    .title(String.format(template.getTitle(), user.getName()))
                    .message(template.getMessage())
                    .build();

            String payload = objectMapper.writeValueAsString(dto);
            log.info("payload: {}", payload);

            notificationHistoriesRepository.save(notification);

            log.info("3");

            if (sseEmitters.containsKey(user.getId())) {
                sseEmitters.get(user.getId()).send(SseEmitter.event()
                        .name("welcome message")
                        .data(payload, MediaType.APPLICATION_JSON));
            }
            log.info("환영 알림 전송 완료 - 사용자: {}", name);

        } catch (Exception e) {
            log.error("환영 알림 전송 실패 - 사용자: {}", name, e);
            throw new NotificationException("환영 알림 전송 실패", e);
        }

    }

    // user 정보 받아와서 결제 완료 알림 생성
    public void sendPaymentCompleteTestNotification(Long userId, String orderName, Long orderPrice) {

        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(()-> new RuntimeException("회원이 없습니다."));

            Notification template = notificationRepository.findById(2L)
                    .orElseThrow(() -> new IllegalArgumentException("결제 완료 알림 템플릿이 없습니다."));

            log.info("1");
            NotificationHistories notification = NotificationHistories.builder()
                    .user(user)
                    .notification(template)
                    .title(template.getTitle())
                    .message(String.format(template.getMessage(), user.getName() ,orderName, orderPrice))
                    .createdAt(Timestamp.valueOf(LocalDateTime.now()))
                    .expiredAt(Timestamp.valueOf(LocalDateTime.now().plusDays(14)))
                    .build();
            log.info("2");
            notificationHistoriesRepository.save(notification);
            log.info("3");
            NotificationDTO dto = NotificationDTO.builder()
                    .title(template.getTitle())
                    .message(String.format(template.getMessage(), user.getName() ,orderName, orderPrice))
                    .build();

            String payload = objectMapper.writeValueAsString(dto);
            log.info("payload: {}", payload);

            if (sseEmitters.containsKey(user.getId())) {
                sseEmitters.get(user.getId()).send(SseEmitter.event()
                        .name("Payment Complete Message")
                        .data(payload, MediaType.APPLICATION_JSON));
                log.info(payload, MediaType.APPLICATION_JSON);
            }

            log.info("결제 완료 알림 전송 완료 - 사용자: {}, 주문: {}, 금액: {}", user.getName(), orderName, orderPrice);
        } catch (Exception e) {
            log.error("결제 완료 알림 전송 실패 - 사용자: {}, 주문: {}, 금액: {}", userId, orderName, orderPrice, e);
            throw new NotificationException("결제 완료 알림 전송 실패", e);
        }

    }

    // user 정보 받아와서 결제 취소 완료 알림 생성
    public void sendPaymentCancelTestNotification(Long userId, String orderName, Long refundPrice) {

        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(()-> new RuntimeException("회원이 없습니다."));

            Notification template = notificationRepository.findById(3L)
                    .orElseThrow(() -> new IllegalArgumentException("결제 취소 알림 템플릿이 없습니다."));


            NotificationHistories notification = NotificationHistories.builder()
                    .user(user)
                    .notification(template)
                    .message(String.format(template.getMessage(), orderName, refundPrice))
                    .createdAt(Timestamp.valueOf(LocalDateTime.now()))
                    .expiredAt(Timestamp.valueOf(LocalDateTime.now().plusDays(14)))
                    .build();

            notificationHistoriesRepository.save(notification);

            if (sseEmitters.containsKey(user.getId())) {
                sseEmitters.get(user.getId()).send(SseEmitter.event()
                        .name("Payment Cancel Complete Message")
                        .data(notification));
            }

            log.info("결제 취소 알림 전송 완료 - 사용자: {}, 주문: {}", userId, orderName);
        } catch (Exception e) {
            log.error("결제 취소 알림 전송 실패 - 사용자: {}, 주문: {}", userId, orderName, e);
            throw new NotificationException("결제 취소 알림 전송 실패", e);
        }


    }

    // user 정보 받아와서 결제 취소 완료 알림 생성
    public void sendPaymentCancelNotification(User user, String orderName, Long refundPrice) {

        try {
            Notification template = notificationRepository.findById(3L)
                    .orElseThrow(() -> new IllegalArgumentException("결제 취소 알림 템플릿이 없습니다."));


            NotificationHistories notification = NotificationHistories.builder()
                    .user(user)
                    .notification(template)
                    .message(String.format(template.getMessage(), orderName, refundPrice))
                    .createdAt(Timestamp.valueOf(LocalDateTime.now()))
                    .expiredAt(Timestamp.valueOf(LocalDateTime.now().plusDays(14)))
                    .build();

            notificationHistoriesRepository.save(notification);

            if (sseEmitters.containsKey(user.getId())) {
                sseEmitters.get(user.getId()).send(SseEmitter.event()
                        .name("Payment Cancel Complete Message")
                        .data(notification));
            }

            log.info("결제 취소 알림 전송 완료 - 사용자: {}, 주문: {}", user.getName(), orderName);
        } catch (Exception e) {
            log.error("결제 취소 알림 전송 실패 - 사용자: {}, 주문: {}", user.getName(), orderName, e);
            throw new NotificationException("결제 취소 알림 전송 실패", e);
        }


    }

    // user 정보 받아와서 장바구니 상품 품절 임박 완료 알림 생성
    public void sendCartLowNotification(User user, String productName) {

        try {
            Notification template = notificationRepository.findById(4L)
                    .orElseThrow(() -> new IllegalArgumentException("재고 알림 템플릿이 없습니다."));

            NotificationHistories notification = NotificationHistories.builder()
                    .user(user)
                    .notification(template)
                    .message(String.format(template.getMessage(), productName))
                    .createdAt(Timestamp.valueOf(LocalDateTime.now()))
                    .expiredAt(Timestamp.valueOf(LocalDateTime.now().plusDays(14)))
                    .build();

            notificationHistoriesRepository.save(notification);

            if (sseEmitters.containsKey(user.getId())) {
                sseEmitters.get(user.getId()).send(SseEmitter.event()
                        .name("Cart Product Stock Low Message")
                        .data(notification));
            }

            log.info("장바구니 재고 알림 전송 완료 - 사용자: {}, 상품: {}", user.getName(), productName);

        } catch (Exception e) {
            log.error("장바구니 재고 알림 전송 실패 - 사용자: {}, 상품: {}", user.getName(), productName, e);
            throw new NotificationException("장바구니 재고 알림 전송 실패", e);
        }


    }

    // user 정보 받아와서 위시리스트 상품 품절 임박 완료 알림 생성
    public void sendWishListLowNotification(User user, String productName) {

        try {
            Notification template = notificationRepository.findById(5L)
                    .orElseThrow(() -> new IllegalArgumentException("위시리스트 알림 템플릿이 없습니다."));

            NotificationHistories notification = NotificationHistories.builder()
                    .user(user)
                    .notification(template)
                    .message(String.format(template.getMessage(), productName))
                    .createdAt(Timestamp.valueOf(LocalDateTime.now()))
                    .expiredAt(Timestamp.valueOf(LocalDateTime.now().plusDays(14)))
                    .build();

            notificationHistoriesRepository.save(notification);

            if (sseEmitters.containsKey(user.getId())) {
                sseEmitters.get(user.getId()).send(SseEmitter.event()
                        .name("WishList Product Stock Low Message")
                        .data(notification));
            }

            log.info("위시리스트 재고 알림 전송 완료 - 사용자: {}, 상품: {}", user.getName(), productName);
        } catch (Exception e) {
            log.error("위시리스트 재고 알림 전송 실패 - 사용자: {}, 상품: {}", user.getName(), productName, e);
            throw new NotificationException("위시리스트 재고 알림 전송 실패", e);
        }
    }


    // user 정보 받아와서 리뷰 요청 완료 알림 생성
    public void sendReviewRequestNotification(User user, String productName) {
        try {
            Notification template = notificationRepository.findById(6L)
                    .orElseThrow(() -> new IllegalArgumentException("리뷰 요청 알림 템플릿이 없습니다."));


            NotificationHistories notification = NotificationHistories.builder()
                    .user(user)
                    .notification(template)
                    .message(String.format(template.getMessage(),user.getName(), productName))
                    .createdAt(Timestamp.valueOf(LocalDateTime.now()))
                    .expiredAt(Timestamp.valueOf(LocalDateTime.now().plusDays(14)))
                    .build();

            if (sseEmitters.containsKey(user.getId())) {
                sseEmitters.get(user.getId()).send(SseEmitter.event()
                        .name("WishList Product Stock Low Message")
                        .data(notification));
            }

            log.info("리뷰 요청 알림 전송 완료 - 사용자: {}, 상품: {}", user.getName(), productName);
        } catch (Exception e) {
//            log.error("리뷰 요청 알림 전송 실패 - 사용자: {}, 상품: {}", user.getName(), productName, e);
            throw new NotificationException("리뷰 요청 알림 전송 실패", e);
        }

    }

    public void sendChatNotification(User user, Long senderId, String senderName) {

        try {
            Notification template = notificationRepository.findById(7L)
                    .orElseThrow(() -> new IllegalArgumentException("채팅 알림 템플릿이 없습니다."));

            NotificationHistories notification = NotificationHistories.builder()
                    .user(user)
                    .notification(template)
                    .message(String.format(template.getMessage(), senderName))
                    .createdAt(Timestamp.valueOf(LocalDateTime.now()))
                    .expiredAt(Timestamp.valueOf(LocalDateTime.now().plusDays(14)))
                    .isRead(false)
                    .build();

            notificationHistoriesRepository.save(notification);

            if (sseEmitters.containsKey(user.getId())) {
                sseEmitters.get(user.getId()).send(SseEmitter.event()
                        .name("Chat Notification Message")
                        .data(notification));
            }
            log.info("채팅 알림 전송 완료 - 사용자: {}, 발신자: {}", user.getName(), senderName);
        } catch (Exception e) {
            log.error("채팅 알림 전송 실패 - 사용자: {}, 발신자: {}", user.getName(), senderName, e);
            throw new NotificationException("채팅 알림 전송 실패", e);
        }
    }
}
