package org.example.sansam.notification.eventListener.email;

import org.example.sansam.notification.event.email.PaymentCanceledEmailEvent;
import org.example.sansam.notification.event.email.PaymentCompleteEmailEvent;
import org.example.sansam.notification.event.email.UserWelcomeEmailEvent;
import org.example.sansam.notification.service.EmailService;
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
class EmailListenerTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private PaymentCompleteEmailEventListener paymentCompleteEmailEventListener;

    @InjectMocks
    private PaymentCanceledEmailEventListener paymentCanceledEmailEventListener;

    @InjectMocks
    private UserWelcomeEmailListener userWelcomeEmailListener;

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

    @Test
    @DisplayName("결제 완료 이벤트 발생 시 메일 전송을 수행한다")
    void handlePaymentCompleted_success() {
        // given
        User user = createUser();
        PaymentCompleteEmailEvent event = new PaymentCompleteEmailEvent(user, "주문1", 50000L);

        // when
        paymentCompleteEmailEventListener.handlePaymentCompleted(event);

        // then
        verify(emailService, times(1))
                .sendPaymentCompletedEmail(user, "주문1", 50000L);
    }

    @Test
    @DisplayName("결제 완료 메일 전송 실패 시 예외는 잡히고 전파되지 않는다")
    void handlePaymentCompleted_fail() {
        // given
        User user = createUser();
        PaymentCompleteEmailEvent event = new PaymentCompleteEmailEvent(user, "주문1", 50000L);

        doThrow(new RuntimeException("SMTP 오류"))
                .when(emailService).sendPaymentCompletedEmail(any(), anyString(), anyLong());

        // when
        paymentCompleteEmailEventListener.handlePaymentCompleted(event);

        // then
        verify(emailService, times(1)).sendPaymentCompletedEmail(user, "주문1", 50000L);
    }

    @Test
    @DisplayName("결제 취소 이벤트 발생 시 메일 전송을 수행한다")
    void handlePaymentCanceled_success() {
        // given
        User user = createUser();
        PaymentCanceledEmailEvent event = new PaymentCanceledEmailEvent(user, "주문2", 20000L);

        // when
        paymentCanceledEmailEventListener.handlePaymentCanceled(event);

        // then
        verify(emailService, times(1))
                .sendPaymentCanceledMessage(user, "주문2", 20000L);
    }

    @Test
    @DisplayName("결제 취소 메일 전송 실패 시 예외는 잡히고 전파되지 않는다")
    void handlePaymentCanceled_fail() {
        // given
        User user = createUser();
        PaymentCanceledEmailEvent event = new PaymentCanceledEmailEvent(user, "주문2", 20000L);

        doThrow(new RuntimeException("SMTP 오류"))
                .when(emailService).sendPaymentCanceledMessage(any(), anyString(), anyLong());

        // when
        paymentCanceledEmailEventListener.handlePaymentCanceled(event);

        // then
        verify(emailService, times(1))
                .sendPaymentCanceledMessage(user, "주문2", 20000L);
    }

    @Test
    @DisplayName("회원가입 환영 이벤트 발생 시 메일 전송을 수행한다")
    void handleUserSignUp_success() {
        // given
        User user = createUser();
        UserWelcomeEmailEvent event = new UserWelcomeEmailEvent(user);

        // when
        userWelcomeEmailListener.handleUserSignUp(event);

        // then
        verify(emailService, times(1))
                .sendWelcomeEmail(user);
    }

    @Test
    @DisplayName("회원가입 환영 메일 전송 실패 시 예외는 잡히고 전파되지 않는다")
    void handleUserSignUp_fail() {
        // given
        User user = createUser();
        UserWelcomeEmailEvent event = new UserWelcomeEmailEvent(user);

        doThrow(new RuntimeException("SMTP 오류"))
                .when(emailService).sendWelcomeEmail(any(User.class));

        // when
        userWelcomeEmailListener.handleUserSignUp(event);

        // then
        verify(emailService, times(1)).sendWelcomeEmail(user);
    }
}