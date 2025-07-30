package org.example.sansam.NotificationTest;

import jakarta.transaction.Transactional;
import org.example.sansam.notification.dto.*;
import org.example.sansam.notification.repository.NotificationHistoryRepository;
import org.example.sansam.notification.repository.NotificationsRepository;
import org.example.sansam.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {
    @Mock
    private NotificationsRepository notificationsRepository;

    @Mock
    private NotificationHistoryRepository notificationHistoryRepository;

    @InjectMocks
    private NotificationService notificationService;

    private SseEmitter emitter;

    @BeforeEach
    void setUp() {
        emitter = new SseEmitter(60L * 1000L); // 60초 타임아웃
    }

    @Test
    void connect_성공() {
        // given
        Long userId = 1L;

        // when
        SseEmitter result = notificationService.connect(userId);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(SseEmitter.class);
    }

    @Test
    void sendWelcomeNotification_성공() {
        // given
        Long userId = 1L;
        UserNotiDTO userNotiDTO = new UserNotiDTO(userId,"테스트 유저");

        // 먼저 SSE 연결 설정
        notificationService.connect(userId);

        // when
        notificationService.sendWelcomeNotification(userId, userNotiDTO);

        // then
        verify(notificationHistoryRepository, times(1)).save(argThat(history ->
                history.getUser_id().equals(userId) &&
                        history.getNotification_id().equals(1L)
        ));

    }

    @Test
    void sendPaymentCompleteNotification_성공() {
        // given
        Long userId = 1L;
        Long orderId = 5L;
        Long finalPrice = 50000L;
        PaymentNotiDTO paymentNotiDTO = new PaymentNotiDTO(
                userId,
                orderId,
                finalPrice
        );

        // 중요: SSE 연결 먼저 설정
        notificationService.connect(userId);

        // when
        assertDoesNotThrow(() ->
                notificationService.sendPaymentCompleteNotification(userId, paymentNotiDTO)
        );

        // then
        verify(notificationsRepository, times(1)).save(any());
        verify(notificationHistoryRepository, times(1)).save(any());
    }

    @Test
    void sendPaymentCancelNotification_성공() {
        // given
        Long userId = 1L;
        Long orderId = 5L;
        Long refundPrice = 50000L;
        PaymentCancelNotiDTO cancelNotiDTO = new PaymentCancelNotiDTO(
                userId,
                orderId,
                refundPrice
        );

        // 중요: SSE 연결 먼저 설정
        notificationService.connect(userId);

        // when
        assertDoesNotThrow(() ->
                notificationService.sendPaymentCancelNotification(userId, cancelNotiDTO)
        );

        // then
        verify(notificationsRepository, times(1)).save(any());
        verify(notificationHistoryRepository, times(1)).save(any());
    }

    @Test
    void sendCartLowNotification_성공() {
        // given
        Long userId = 1L;
        Long productId = 5L;
        String productName = "오프화이트 반팔";
        CartNotiDTO cartNotiDTO = new CartNotiDTO(
                productId,
                productName // 남은 수량
        );

        // 중요: SSE 연결 먼저 설정
        notificationService.connect(userId);

        // when
        assertDoesNotThrow(() ->
                notificationService.sendCartLowNotification(userId, cartNotiDTO)
        );

        // then
        verify(notificationsRepository, times(1)).save(any());
        verify(notificationHistoryRepository, times(1)).save(any());
    }

    @Test
    void sendWishListLowNotification_성공() {
        // given
        Long userId = 1L;
        Long productId = 5L;
        String productName = "오프화이트 반팔";
        WishListNotiDTO wishListNotiDTO = new WishListNotiDTO(
                productId,
                productName // 남은 수량
        );

        // 중요: SSE 연결 먼저 설정
        notificationService.connect(userId);

        // when
        assertDoesNotThrow(() ->
                notificationService.sendWishListLowNotification(userId, wishListNotiDTO)
        );

        // then
        verify(notificationsRepository, times(1)).save(any());
        verify(notificationHistoryRepository, times(1)).save(any());
    }

    @Test
    void sendReviewRequestNotification_성공() {
        // given
        Long userId = 1L;
        Long productId = 5L;
        String productName = "오프화이트 반팔";
        ReviewNotiDTO reviewNotiDTO = new ReviewNotiDTO(
                productId,
                productName
        );

        // 중요: SSE 연결 먼저 설정
        notificationService.connect(userId);

        // when
        assertDoesNotThrow(() ->
                notificationService.sendReviewRequestNotification(userId, reviewNotiDTO)
        );

        // then
        verify(notificationsRepository, times(1)).save(any());
        verify(notificationHistoryRepository, times(1)).save(any());
    }

    @Test
    void sendChatNotification_성공() {
        // given
        Long userId = 1L;
        Long senderId = 2L;
        String senderName = "테스트 아무개";
        String message = "테스트용 메시지";
        ChatNotiDTO chatNotiDTO = new ChatNotiDTO(
                senderId,
                senderName,
                message
        );

        // 중요: SSE 연결 먼저 설정
        notificationService.connect(userId);

        // when
        assertDoesNotThrow(() ->
                notificationService.sendChatNotification(userId, chatNotiDTO)
        );

        // then
        verify(notificationsRepository, times(1)).save(any());
        verify(notificationHistoryRepository, times(1)).save(any());
    }

    @Test
    void sendDynamicNotification_성공() {
        // given
        Long userId = 1L;
        Object notificationData = new Object();

        // 중요: SSE 연결 먼저 설정
        notificationService.connect(userId);

        // when
        assertDoesNotThrow(() ->
                notificationService.sendDynamicNotification(userId, notificationData)
        );

        // then
        verify(notificationsRepository, times(1)).save(any());
        verify(notificationHistoryRepository, times(1)).save(any());
    }

    // 실패 케이스 테스트
    @Test
    void connect_실패_잘못된사용자ID() {
        // given
        Long invalidUserId = -1L;

        // when & then
        assertThrows(IllegalArgumentException.class, () ->
                notificationService.connect(invalidUserId)
        );
    }

    @Test
    void sendNotification_실패_저장소예외() {
        // given
        Long userId = 1L;
        UserNotiDTO userNotiDTO = new UserNotiDTO(userId, "테스트 유저");
        when(notificationsRepository.save(any()))
                .thenThrow(new RuntimeException("저장소 에러"));

        // when & then
        assertThrows(RuntimeException.class, () ->
                notificationService.sendWelcomeNotification(userId, userNotiDTO)
        );
    }
}