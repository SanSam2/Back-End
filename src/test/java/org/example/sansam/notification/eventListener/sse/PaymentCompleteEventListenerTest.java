package org.example.sansam.notification.eventListener.sse;

import org.example.sansam.notification.event.sse.PaymentCompleteEvent;
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
class PaymentCompleteEventListenerTest {


    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private PaymentCompleteEventListener paymentCompleteEventListener;

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

    @DisplayName("결제 완료 이벤트가 발생하면 알림 서비스가 호출된다.")
    @Test
    void handlePaymentCompleteEvent_when_event_is_payment_complete_then_call_notification_service() {
        // given
        User user = createUser();
        PaymentCompleteEvent event = new PaymentCompleteEvent(user,"운동화 주문",12000L);

        // when
        paymentCompleteEventListener.handelPaymentCompleteEvent(event);

        // then
        verify(notificationService, times(1))
                .sendPaymentCompleteNotification(user, "운동화 주문",12000L);
    }

    @DisplayName("결제 완료 알림 전송 중 예외가 발생하면 예외가 전파된다.")
    @Test
    void handlePaymentCompleteEvent_when_exception_occurs_during_notification_then_exception_is_thrown() {
        // given
        User user = createUser();
        PaymentCompleteEvent event = new PaymentCompleteEvent(user, "운동화 주문",12000L);

        doThrow(new RuntimeException("알림 실패"))
                .when(notificationService)
                .sendPaymentCompleteNotification(user, "운동화 주문",12000L);
        // when
        paymentCompleteEventListener.handelPaymentCompleteEvent(event);

        // then
        verify(notificationService, times(1))
                .sendPaymentCompleteNotification(user, "운동화 주문",12000L);
    }
}