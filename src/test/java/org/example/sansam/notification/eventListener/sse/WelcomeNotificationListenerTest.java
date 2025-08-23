package org.example.sansam.notification.eventListener.sse;

import org.example.sansam.notification.event.sse.WelcomeNotificationEvent;
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
class WelcomeNotificationListenerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private WelcomeNotificationListener listener;

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

    @DisplayName("회원가입 환영 이벤트 발생 시 알림을 전송한다")
    @Test
    void handleWelcomeEvent_when_event_is_welcome_then_send_notification() {
        // given
        User user = createUser();
        WelcomeNotificationEvent event = new WelcomeNotificationEvent(user);
        // when
        listener.handelWelcomeNotificationEvent(event);
        // then
        verify(notificationService, times(1))
                .sendWelcomeNotification(user);
    }

    @DisplayName("알림 전송 중 예외 발생 시 로깅하고 예외를 전파하지 않는다")
    @Test
    void handleWelcomeEvent_when_exception_occurs_during_notification_then_exception_is_not_thrown() {
        // given
        User user = createUser();
        WelcomeNotificationEvent event = new WelcomeNotificationEvent(user);

        doThrow(new RuntimeException("뭔 오류"))
                .when(notificationService)
                        .sendWelcomeNotification(any(User.class));
        // when
        listener.handelWelcomeNotificationEvent(event);

        // then
        verify(notificationService, times(1))
                .sendWelcomeNotification(user);
    }

}