package org.example.sansam.notification.service;

import org.assertj.core.api.Assertions;
import org.assertj.core.groups.Tuple;
import org.example.sansam.notification.domain.Notification;
import org.example.sansam.notification.domain.NotificationHistories;
import org.example.sansam.notification.domain.NotificationType;
import org.example.sansam.notification.dto.NotificationDTO;
import org.example.sansam.notification.repository.NotificationHistoriesRepository;
import org.example.sansam.notification.repository.NotificationsRepository;
import org.example.sansam.user.domain.Role;
import org.example.sansam.user.domain.User;
import org.example.sansam.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;


@ActiveProfiles("test")
@SpringBootTest
@Transactional
class NotificationServiceTest {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationHistoriesRepository notificationHistoriesRepository;

    @Autowired
    private NotificationsRepository notificationsRepository;

    @Autowired
    private UserRepository userRepository;

//    @AfterEach
//    void tearDown() {
//        notificationHistoriesRepository.deleteAllInBatch();
//    }

    @DisplayName("사용자는 로그인 했을 시 본인의 알림 목록을 가져온다.")
    @Test
    void getNotificationHistories() {
        // given
        User user = createUser();
        userRepository.save(user);

        Notification notification = notificationsRepository.findById(NotificationType.WELCOME.getTemplateId()).orElseThrow();
        String title = notification.getTitle();
        String message = notification.getMessage();

        NotificationHistories nh1 = createNotification(user, title, message, notification);
        NotificationHistories nh2 = createNotification(user, title, message, notification);

        notificationHistoriesRepository.saveAll(List.of(nh1, nh2));


        // when
        List<NotificationDTO> getList = notificationService.getNotificationHistories(user.getId());

        // then
        Assertions.assertThat(getList).hasSize(2);
    }

    @DisplayName("알림이 하나도 쌓여있지 않을 경우 알림 목록은 없어야 한다.")
    @Test
    void getNotificationHistories_when_no_notification_exists() {
        // given
        User user = createUser();
        userRepository.save(user);

        // when
        List<NotificationHistories> nh1 = notificationHistoriesRepository.findAllByUser_Id(user.getId());

        // then

        Assertions.assertThat(nh1).isEmpty();

    }

    @DisplayName("사용자는 읽지 않은 알림의 개수를 볼 수 있다.")
    @Test
    void getUnreadNotificationCount() {
        // given
        User user = createUser();
        userRepository.save(user);

        Notification notification = notificationsRepository.findById(NotificationType.WELCOME.getTemplateId()).orElseThrow();
        String title = notification.getTitle();
        String message = notification.getMessage();

        NotificationHistories nh1 = createNotification(user, title, message, notification);
        NotificationHistories nh2 = createNotification(user, title, message, notification);

        notificationHistoriesRepository.saveAll(List.of(nh1, nh2));

        // when

        long count = notificationService.getUnreadNotificationCount(user.getId());

        // then

        Assertions.assertThat(count).isEqualTo(2);
    }

    @DisplayName("사용자는 읽지 않은 하나의 알림을 읽음 처리한다.")
    @Test
    void markAsRead() {
        // given
        User user = createUser();
        userRepository.save(user);

        Notification notification = notificationsRepository.findById(NotificationType.WELCOME.getTemplateId()).orElseThrow();
        String title = notification.getTitle();
        String message = notification.getMessage();

        NotificationHistories nh1 = createNotification(user, title, message, notification);

        notificationHistoriesRepository.save(nh1);

        // when

        notificationService.markAsRead(nh1.getId());

        // then

        Assertions.assertThat(notificationHistoriesRepository.findById(nh1.getId()))
                .get()
                .extracting("isRead").isEqualTo(true);
    }

    @DisplayName("사용자는 읽지 않은 알림을 모두 읽음 처리한다.")
    @Test
    void markAllAsRead() {
        // given

        User user = createUser();
        userRepository.save(user);

        Notification notification = notificationsRepository.findById(NotificationType.WELCOME.getTemplateId()).orElseThrow();
        String title = notification.getTitle();
        String message = notification.getMessage();

        NotificationHistories nh1 = createNotification(user, title, message, notification);
        NotificationHistories nh2 = createNotification(user, title, message, notification);

        notificationHistoriesRepository.saveAll(List.of(nh1, nh2));

        // when
        notificationService.markAllAsRead(user.getId());

        // then
        Assertions.assertThat(notificationHistoriesRepository.countByUser_IdAndIsReadFalse(user.getId()))
                .isEqualTo(0);
    }

    @DisplayName("사용자는 본인의 알림을 개별로 삭제할 수 있다.")
    @Test
    void deleteNotificationHistory() {
        // given
        User user = createUser();
        userRepository.save(user);

        Notification notification = notificationsRepository.findById(NotificationType.WELCOME.getTemplateId()).orElseThrow();
        String title = notification.getTitle();
        String message = notification.getMessage();

        NotificationHistories nh1 = createNotification(user, title, message, notification);
        NotificationHistories nh2 = createNotification(user, title, message, notification);

        notificationHistoriesRepository.saveAll(List.of(nh1, nh2));

        // when
        notificationService.deleteNotificationHistory(user.getId(), nh1.getId());

        // then
        Assertions.assertThat(notificationHistoriesRepository.findAllByUser_Id(user.getId()))
                .hasSize(1)
                .extracting(NotificationHistories::getMessage)
                .containsExactlyInAnyOrder(message);
    }

    @DisplayName("회원 가입 환영 알림을 사용자한테 전송한다")
    @Test
    void sendWelcomeNotification_success() {
        // given
        User user = createUser();
        userRepository.save(user);

        // when
        notificationService.sendWelcomeNotification(user);
        // then

        Assertions.assertThat(notificationHistoriesRepository.findAllByUser_Id(user.getId()))
                .hasSize(1)
                .extracting("title", "message")
                .containsExactlyInAnyOrder(
                        Tuple.tuple("호상 테스트님 가입을 환영합니다.", "고객님의 스타일을 책임져주는 Or de Firenz의 회원이 되신 것을 환영합니다.")
                );

    }

    @DisplayName("결제 완료 알림을 사용자한테 전송한다.")
    @Test
    void sendPaymentNotification_success() {
        // given
        User user = createUser();
        userRepository.save(user);

        String orderName = "나이키 운동화";
        Long orderPrice = 10000000L;
        // when
        notificationService.sendPaymentCompleteNotification(user, orderName, orderPrice);

        // then
        assertThat(notificationHistoriesRepository.findAllByUser_Id(user.getId()))
                .hasSize(1)
                .extracting("title", "message")
                .containsExactlyInAnyOrder(
                        Tuple.tuple("결제 완료", "호상 테스트님의 나이키 운동화 10000000원 결제 완료되었습니다.")
                );
    }

    @DisplayName("결제 취소 알림을 사용자한테 전송한다.")
    @Test
    void sendPaymentCancelNotification_success() {
        // given
        User user = createUser();
        userRepository.save(user);

        String orderName = "아디다스 티셔츠";
        Long refundPrice = 10000000L;
        // when
        notificationService.sendPaymentCancelNotification(user, orderName, refundPrice);

        // then
        assertThat(notificationHistoriesRepository.findAllByUser_Id(user.getId()))
                .hasSize(1)
                .extracting("title", "message")
                .containsExactlyInAnyOrder(
                        Tuple.tuple("결제 취소", "호상 테스트님의 아디다스 티셔츠 10000000원 결제 취소되었습니다.")
                );
    }

    @DisplayName("장바구니 재고 임박 알림을 사용자한테 전송한다.")
    @Test
    void sendLowStockNotification_success() {
        // given
        User user = createUser();
        userRepository.save(user);

        String productName = "무신사 반팔티";
        // when
        notificationService.sendCartLowNotification(user, productName);

        // then
        assertThat(notificationHistoriesRepository.findAllByUser_Id(user.getId()))
                .hasSize(1)
                .extracting("title", "message")
                .containsExactlyInAnyOrder(
                        Tuple.tuple("장바구니 상품 품절", "장바구니에 담아놓은 무신사 반팔티의 재고가 얼마 남지 않았습니다.")
                );
    }

    @DisplayName("위시리스트 재고 임박 알림을 사용자한테 전송한다.")
    @Test
    void sendLowWishListNotification_success() {
        // given
        User user = createUser();
        userRepository.save(user);

        String productName = "뉴발란스 530";
        // when
        notificationService.sendWishListLowNotification(user, productName);

        // then
        assertThat(notificationHistoriesRepository.findAllByUser_Id(user.getId()))
                .hasSize(1)
                .extracting("title", "message")
                .containsExactlyInAnyOrder(
                        Tuple.tuple("위시리스트 상품 품절", "위시리스트에 담아놓은 뉴발란스 530의 재고가 얼마 남지 않았습니다.")
                );
    }

    @DisplayName("리뷰 요청 알림을 사용자한테 전송한다.")
    @Test
    void sendReviewRequestNotification_success() {
        // given
        User user = createUser();
        userRepository.save(user);

        String orderName = "스투시 반팔티";
        // when
        notificationService.sendReviewRequestNotification(user, orderName);

        // then

        assertThat(notificationHistoriesRepository.findAllByUser_Id(user.getId()))
                .hasSize(1)
                .extracting("title", "message")
                .containsExactlyInAnyOrder(
                        Tuple.tuple("상품 후기를 작성해보세요.", "Or de Firenz는 호상 테스트님의 소중한 리뷰를 기다리고 있습니다. 주문명 : 스투시 반팔티")
                );
    }

    @DisplayName("채팅 알림을 사용자한테 전송한다.")
    @Test
    void sendChatNotification_success() {
        // given
        User user = createUser();
        userRepository.save(user);

        String chatRoomName = "채팅방 1";
        String message = "안녕하세요!";
        // when
        notificationService.sendChatNotification(user, chatRoomName, message);

        // then

        assertThat(notificationHistoriesRepository.findAllByUser_Id(user.getId()))
                .hasSize(1)
                .extracting("title", "message")
                .containsExactlyInAnyOrder(
                        Tuple.tuple("채팅방 1에서 메시지가 도착했습니다.", "안녕하세요!")
                );
    }

    private User createUser() {
        return User.builder()
                .email("dvbf@naver.com")
                .name("호상 테스트")
                .password("1004")
                .mobileNumber("01012345678")
                .role(Role.USER)
                .salary(10000000L)
                .createdAt(LocalDateTime.now())
                .activated(true)
                .emailAgree(true)
                .build();
    }

    private NotificationHistories createNotification(User user, String title, String message, Notification notification) {
        return notificationHistoriesRepository.save(
                NotificationHistories.builder()
                        .user(user)
                        .title(String.format(title, user.getName()))
                        .message(message)
                        .createdAt(LocalDateTime.now())
                        .notification(notification)
                        .isRead(false)
                        .expiredAt(LocalDateTime.now().plusDays(14))
                        .build());
    }
}