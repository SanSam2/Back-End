package org.example.sansam.notification.service;

import org.example.sansam.notification.domain.Notification;
import org.example.sansam.notification.domain.NotificationHistories;
import org.example.sansam.notification.domain.NotificationType;
import org.example.sansam.notification.dto.NotificationDTO;
import org.example.sansam.notification.event.sse.BroadcastEvent;
import org.example.sansam.notification.event.sse.NotificationSavedEvent;
import org.example.sansam.notification.infra.SseConnector;
import org.example.sansam.notification.infra.SseProvider;
import org.example.sansam.notification.repository.NotificationHistoriesRepository;
import org.example.sansam.notification.repository.NotificationsRepository;
import org.example.sansam.user.domain.Role;
import org.example.sansam.user.domain.User;
import org.example.sansam.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;


@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
@Transactional
class NotificationServiceTest {

    @Mock
    private NotificationHistoriesRepository notificationHistoriesRepository;

    @Mock
    private NotificationsRepository notificationsRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApplicationEventPublisher publisher;

    @Mock
    private SseConnector sseConnector;

    @Mock
    private SseProvider sseProvider;

    @Mock
    private BroadcastInsertService broadcastInsertService;

    @InjectMocks
    private NotificationService notificationService;

    @Mock
    private NotificationHistoryReader notificationHistoryReader;

    private User user;

    @BeforeEach
    void setUp() {
        user = createUser(1L, "test@naver.com");
    }

    private User createUser(Long id, String email) {
        return User.builder()
                .id(id)
                .email(email)
                .name("호상 테스트")
                .password("1234")
                .mobileNumber("01012345678")
                .role(Role.USER)
                .salary(1000000L)
                .createdAt(LocalDateTime.now())
                .activated(true)
                .emailAgree(true)
                .build();
    }

    private NotificationHistories createHistory(User user, Notification template, String title, String message) {
        return NotificationHistories.builder()
                .id(1L)
                .user(user)
                .notification(template)
                .eventName("welcome")
                .title(title)
                .message(message)
                .createdAt(LocalDateTime.now())
                .expiredAt(LocalDateTime.now().plusDays(14))
                .isRead(false)
                .build();
    }

    private Notification mockTemplate(NotificationType type) {
        return switch (type) {
            case WELCOME -> new Notification(type.getTemplateId(),
                    "%s님 가입을 환영합니다.",
                    "고객님의 스타일을 책임져주는 Or de Firenz의 회원이 되신 것을 환영합니다.");
            case PAYMENT_COMPLETE -> new Notification(type.getTemplateId(),
                    "결제 완료",
                    "%s님의 %s %s원 결제 완료되었습니다.");
            case PAYMENT_CANCEL -> new Notification(type.getTemplateId(),
                    "결제 취소",
                    "%s님의 %s %s원 결제 취소되었습니다.");
            case CART_LOW -> new Notification(type.getTemplateId(),
                    "장바구니 상품 품절",
                    "장바구니에 담아놓은 %s의 재고가 얼마 남지 않았습니다.");
            case WISH_LOW -> new Notification(type.getTemplateId(),
                    "위시리스트 상품 품절",
                    "위시리스트에 담아놓은 %s의 재고가 얼마 남지 않았습니다.");
            case REVIEW_REQUEST -> new Notification(type.getTemplateId(),
                    "상품 후기를 작성해보세요.",
                    "Or de Firenz는 %s님의 소중한 리뷰를 기다리고 있습니다. 주문명 : %s");
            case CHAT -> new Notification(type.getTemplateId(),
                    "%s에서 메시지가 도착했습니다.",
                    "%s");
            case BROADCAST -> new Notification(type.getTemplateId(),
                    "%s",
                    "%s");
        };
    }

    @DisplayName("사용자는 로그인 했을 시 본인의 알림 목록을 가져온다.")
    @Test
    void getNotificationHistories() {
        NotificationHistories nh1 = createHistory(user, null, "t1", "m1");
        NotificationHistories nh2 = createHistory(user, null, "t2", "m2");
        when(notificationHistoriesRepository.findAllByUser_Id(user.getId()))
                .thenReturn(List.of(nh1, nh2));

        List<NotificationDTO> result = notificationService.getNotificationHistories(user.getId());

        assertThat(result).hasSize(2);
    }

    @DisplayName("사용자는 읽지 않은 알림의 개수를 볼 수 있다.")
    @Test
    void getUnreadNotificationCount() {
        when(notificationHistoriesRepository.countByUser_IdAndIsReadFalse(user.getId()))
                .thenReturn(3L);

        Long count = notificationService.getUnreadNotificationCount(user.getId());

        assertThat(count).isEqualTo(3L);
    }

    @DisplayName("사용자는 알림을 읽음 처리할 수 있다.")
    @Test
    void markAsRead() {
        notificationService.markAsRead(10L);

        verify(notificationHistoriesRepository).findByIsReadFalse(10L);
    }

    @DisplayName("사용자는 모든 알림을 읽음 처리할 수 있다.")
    @Test
    void markAllAsRead() {
        notificationService.markAllAsRead(user.getId());

        verify(notificationHistoriesRepository).findAllByUser_IdAndIsReadFalse(user.getId());
    }

    @DisplayName("사용자는 알림을 개별 삭제할 수 있다.")
    @Test
    void deleteNotificationHistory() {
        notificationService.deleteNotificationHistory(user.getId(), 20L);

        verify(notificationHistoriesRepository).deleteByUser_IdAndId(user.getId(), 20L);
    }

    @DisplayName("subscribe 시 lastEventId 이후 알림이 있으면 resend 호출된다.")
    @Test
    void subscribe_withLastEventId_resendsMissed() {
        SseEmitter emitter = new SseEmitter();
        Notification template = mockTemplate(NotificationType.WELCOME);
        NotificationHistories nh = createHistory(user, template, "t", "m");

        when(sseConnector.connect(user.getId())).thenReturn(emitter);
        when(notificationHistoryReader.getMissedHistories(user.getId(), "5"))
                .thenReturn(List.of(nh));

        SseEmitter result = notificationService.subscribe(user.getId(), "5");

        assertThat(result).isNotNull();
        verify(sseProvider).resend(eq(user.getId()), anyList());
    }

    @DisplayName("회원가입 시 환영 알림이 저장 및 이벤트가 발행된다.")
    @Test
    void sendWelcomeNotification() {
        Notification template = mockTemplate(NotificationType.WELCOME);
        when(notificationsRepository.findById(NotificationType.WELCOME.getTemplateId()))
                .thenReturn(Optional.of(template));
        when(notificationHistoriesRepository.save(any(NotificationHistories.class)))
                .thenAnswer(inv -> {
                    NotificationHistories nh = inv.getArgument(0);
                    nh.setId(1L); // 직렬화용 id 보정
                    return nh;
                });

        notificationService.sendWelcomeNotification(user);

        verify(notificationHistoriesRepository).save(any(NotificationHistories.class));
        verify(publisher).publishEvent(any(NotificationSavedEvent.class));
    }

    @DisplayName("결제 완료 알림이 저장 및 이벤트가 발행된다.")
    @Test
    void sendPaymentCompleteNotification() {
        Notification template = mockTemplate(NotificationType.PAYMENT_COMPLETE);
        when(notificationsRepository.findById(NotificationType.PAYMENT_COMPLETE.getTemplateId()))
                .thenReturn(Optional.of(template));
        when(notificationHistoriesRepository.save(any())).thenAnswer(inv -> {
            NotificationHistories nh = inv.getArgument(0);
            nh.setId(1L);
            return nh;
        });

        notificationService.sendPaymentCompleteNotification(user, "주문1", 10000L);

        verify(notificationHistoriesRepository).save(any());
        verify(publisher).publishEvent(any(NotificationSavedEvent.class));
    }

    @DisplayName("결제 취소 알림이 저장 및 이벤트가 발행된다.")
    @Test
    void sendPaymentCancelNotification() {
        Notification template = mockTemplate(NotificationType.PAYMENT_CANCEL);
        when(notificationsRepository.findById(NotificationType.PAYMENT_CANCEL.getTemplateId()))
                .thenReturn(Optional.of(template));
        when(notificationHistoriesRepository.save(any())).thenAnswer(inv -> {
            NotificationHistories nh = inv.getArgument(0);
            nh.setId(1L);
            return nh;
        });

        notificationService.sendPaymentCancelNotification(user, "주문2", 5000L);

        verify(notificationHistoriesRepository).save(any());
        verify(publisher).publishEvent(any(NotificationSavedEvent.class));
    }

    @DisplayName("장바구니 품절 임박 알림이 저장 및 이벤트가 발행된다.")
    @Test
    void sendCartLowNotification() {
        Notification template = mockTemplate(NotificationType.CART_LOW);
        when(notificationsRepository.findById(NotificationType.CART_LOW.getTemplateId()))
                .thenReturn(Optional.of(template));
        when(notificationHistoriesRepository.save(any())).thenAnswer(inv -> {
            NotificationHistories nh = inv.getArgument(0);
            nh.setId(1L);
            return nh;
        });

        notificationService.sendCartLowNotification(user, "상품A");

        verify(notificationHistoriesRepository).save(any());
        verify(publisher).publishEvent(any(NotificationSavedEvent.class));
    }

    @DisplayName("위시리스트 품절 임박 알림이 저장 및 이벤트가 발행된다.")
    @Test
    void sendWishListLowNotification() {
        Notification template = mockTemplate(NotificationType.WISH_LOW);
        when(notificationsRepository.findById(NotificationType.WISH_LOW.getTemplateId()))
                .thenReturn(Optional.of(template));
        when(notificationHistoriesRepository.save(any())).thenAnswer(inv -> {
            NotificationHistories nh = inv.getArgument(0);
            nh.setId(1L);
            return nh;
        });

        notificationService.sendWishListLowNotification(user, "상품B");

        verify(notificationHistoriesRepository).save(any());
        verify(publisher).publishEvent(any(NotificationSavedEvent.class));
    }

    @DisplayName("리뷰 요청 알림이 저장 및 이벤트가 발행된다.")
    @Test
    void sendReviewRequestNotification() {
        Notification template = mockTemplate(NotificationType.REVIEW_REQUEST);
        when(notificationsRepository.findById(NotificationType.REVIEW_REQUEST.getTemplateId()))
                .thenReturn(Optional.of(template));
        when(notificationHistoriesRepository.save(any())).thenAnswer(inv -> {
            NotificationHistories nh = inv.getArgument(0);
            nh.setId(1L);
            return nh;
        });

        notificationService.sendReviewRequestNotification(user, "주문C");

        verify(notificationHistoriesRepository).save(any());
        verify(publisher).publishEvent(any(NotificationSavedEvent.class));
    }

    @DisplayName("채팅 알림이 저장 및 이벤트가 발행된다.")
    @Test
    void sendChatNotification() {
        Notification template = mockTemplate(NotificationType.CHAT);
        when(notificationsRepository.findById(NotificationType.CHAT.getTemplateId()))
                .thenReturn(Optional.of(template));
        when(notificationHistoriesRepository.save(any())).thenAnswer(inv -> {
            NotificationHistories nh = inv.getArgument(0);
            nh.setId(1L);
            return nh;
        });

        notificationService.sendChatNotification(user, "채팅방1", "메시지");

        verify(notificationHistoriesRepository).save(any());
        verify(publisher).publishEvent(any(NotificationSavedEvent.class));
    }

    @DisplayName("Broadcast 알림은 모든 활성화 유저에게 저장되고 이벤트가 발행된다.")
    @Test
    void saveBroadcastNotification() {
        Notification template = mockTemplate(NotificationType.BROADCAST);
        when(notificationsRepository.findById(NotificationType.BROADCAST.getTemplateId()))
                .thenReturn(Optional.of(template));
        when(userRepository.findAllByActivated(true)).thenReturn(List.of(user));

        NotificationHistories lastSaved = createHistory(user, template, "공지 제목", "공지 내용");
        when(notificationHistoryReader.getLastBroadcastHistory())
                .thenReturn(lastSaved);

        notificationService.saveBroadcastNotification("공지 제목", "공지 내용", LocalDateTime.now());

        verify(broadcastInsertService).saveBroadcastNotification(anyList());
        verify(publisher).publishEvent(any(BroadcastEvent.class));
    }
}