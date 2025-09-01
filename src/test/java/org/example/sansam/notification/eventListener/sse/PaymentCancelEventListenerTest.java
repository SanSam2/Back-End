package org.example.sansam.notification.eventListener.sse;

import org.example.sansam.notification.event.sse.PaymentCancelEvent;
import org.example.sansam.notification.service.NotificationService;
import org.example.sansam.user.domain.Role;
import org.example.sansam.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class PaymentCancelEventListenerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private PaymentCancelEventListener listener;


    private User createUser() {
        return User.builder()
                .id(1L)
                .email("zm@gmail.com")
                .name("테스트1")
                .password("1004")
                .mobileNumber("01012345678")
                .role(Role.USER)
                .salary(10000000L)
                .createdAt(LocalDateTime.now())
                .activated(true)
                .emailAgree(true)
                .build();
    }

    @DisplayName("결제 취소 이벤트를 수신하면 알림을 전송한다")
    @Test
    void handlePaymentCancelEvent_when_event_is_payment_cancel_then_send_notification() {
        // given
        User user = createUser();
        PaymentCancelEvent event = new PaymentCancelEvent(user, "주문1", 10000L);

        // when
        listener.handelPaymentCancelEvent(event);

        // then
        verify(notificationService, times(1))
                .sendPaymentCancelNotification(user, "주문1", 10000L);
    }

    @DisplayName("알림 전송 중 예외 발생 시 로깅하고 예외를 전파하지 않는다")
    @Test
    void handlePaymentCancelEvent_when_exception_occurs_during_notification_then_exception_is_not_thrown() {
        // given
        User user = createUser();
        PaymentCancelEvent event = new PaymentCancelEvent(user, "주문1", 10000L);

        doThrow(new RuntimeException("뭔 오류"))
                .when(notificationService)
                .sendPaymentCancelNotification(any(), anyString(), anyLong());
        // when
        listener.handelPaymentCancelEvent(event);

        // then
        verify(notificationService, times(1))
                .sendPaymentCancelNotification(user, "주문1", 10000L);
    }

}