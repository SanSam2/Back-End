package org.example.sansam.NotificationTest;

import org.example.sansam.notification.domain.Notification;
import org.example.sansam.notification.repository.NotificationHistoryRepository;
import org.example.sansam.notification.repository.NotificationsRepository;
import org.example.sansam.notification.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.sql.Timestamp;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {
    @Mock
    private NotificationsRepository notificationsRepository;

    @Mock
    private NotificationHistoryRepository notificationHistoryRepository;

    @InjectMocks
    private NotificationService notificationService;

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
        String username = "테스트 유저";
        
        // notification 템플릿 설정
        when(notificationsRepository.findByNotificationId(1L))
            .thenReturn(Optional.of(Notification.builder()
                .id(1L)
                .title("테스트 알림")
                .message("테스트 메시지 %s")
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .build()));

        // SSE 연결 설정
        notificationService.connect(userId);

        // when
        notificationService.sendWelcomeNotification(userId, username);

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
        String orderName = "테스트 주문";
        Long orderPrice = 50000L;

        // SSE 연결 설정
        notificationService.connect(userId);

        // when
        notificationService.sendPaymentCompleteNotification(userId, orderName, orderPrice);

        // then
        verify(notificationHistoryRepository, times(1)).save(argThat(history ->
            history.getUser_id().equals(userId) &&
            history.getNotification_id().equals(2L)
        ));
    }

    @Test
    void sendPaymentCancelNotification_성공() {
        // given
        Long userId = 1L;
        String orderName = "취소된 주문";
        Long refundPrice = 50000L;

        // SSE 연결 설정
        notificationService.connect(userId);

        // when
        notificationService.sendPaymentCancelNotification(userId, orderName, refundPrice);

        // then
        verify(notificationHistoryRepository, times(1)).save(argThat(history ->
            history.getUser_id().equals(userId) &&
            history.getNotification_id().equals(3L)
        ));
    }

    @Test
    void sendCartLowNotification_성공() {
        // given
        Long userId = 1L;
        String productName = "오프화이트 반팔";

        // SSE 연결 설정
        notificationService.connect(userId);

        // when
        notificationService.sendCartLowNotification(userId, productName);

        // then
        verify(notificationHistoryRepository, times(1)).save(argThat(history ->
            history.getUser_id().equals(userId) &&
            history.getNotification_id().equals(4L)
        ));
    }

    @Test
    void sendWishListLowNotification_성공() {
        // given
        Long userId = 1L;
        String productName = "오프화이트 반팔";

        // SSE 연결 설정
        notificationService.connect(userId);

        // when
        notificationService.sendWishListLowNotification(userId, productName);

        // then
        verify(notificationHistoryRepository, times(1)).save(argThat(history ->
            history.getUser_id().equals(userId) &&
            history.getNotification_id().equals(5L)
        ));
    }

    @Test
    void sendReviewRequestNotification_성공() {
        // given
        Long userId = 1L;
        String username = "테스트 유저";
        String productName = "오프화이트 반팔";

        // SSE 연결 설정
        notificationService.connect(userId);

        // when
        notificationService.sendReviewRequestNotification(userId, username, productName);

        // then
        verify(notificationHistoryRepository, times(1)).save(argThat(history ->
            history.getUser_id().equals(userId) &&
            history.getNotification_id().equals(6L)
        ));
    }

    @Test
    void sendChatNotification_성공() {
        // given
        Long userId = 1L;
        Long senderId = 2L;
        String senderName = "테스트 아무개";

        // SSE 연결 설정
        notificationService.connect(userId);

        // when
        notificationService.sendChatNotification(userId, senderId, senderName);

        // then
        verify(notificationHistoryRepository, times(1)).save(argThat(history ->
            history.getUser_id().equals(userId) &&
            history.getNotification_id().equals(7L)
        ));
    }

    // 실패 케이스 테스트
    @Test
    void sendNotification_실패_SSE연결안됨() {
        // given
        Long userId = 1L;
        String username = "테스트 유저";

        // when & then
        assertThrows(NullPointerException.class, () ->
            notificationService.sendWelcomeNotification(userId, username)
        );
    }

    @Test
    void sendNotification_실패_메시지템플릿없음() {
        // given
        Long userId = 1L;
        String username = "테스트 유저";
        when(notificationsRepository.findByNotificationId(anyLong()))
            .thenReturn(Optional.empty());

        // SSE 연결 설정
        notificationService.connect(userId);

        // when
        notificationService.sendWelcomeNotification(userId, username);

        // then
        verify(notificationHistoryRepository, times(1)).save(argThat(history ->
                history.getMessage().contains("템플릿을 가져올 수 없습니다")
        ));
    }
}