package org.example.sansam.notification.repository;

import org.assertj.core.api.Assertions;
import org.example.sansam.notification.domain.Notification;
import org.example.sansam.notification.domain.NotificationHistories;
import org.example.sansam.notification.domain.NotificationType;
import org.example.sansam.user.domain.Role;
import org.example.sansam.user.domain.User;
import org.example.sansam.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class NotificationHistoriesRepositoryTest {

    @Autowired
    private NotificationHistoriesRepository notificationHistoriesRepository;

    @Autowired
    private NotificationsRepository notificationsRepository;

    @Autowired
    private UserRepository userRepository;

//    @DisplayName("")
//    @Test
//    void deleteByExpiredAtBefore(){
//        // given
//
//        // when
//
//        // then
//    }

    @DisplayName("사용자는 본인의 알림 목록을 가져올 수 있다.")
    @Test
    void findAllNotificationsByUser_Id() {
        // given
        User user = createUser("qpqp@naver.com", "테스트용1", "1004", "01012345678", Role.USER, 10000000L, LocalDateTime.now());
        userRepository.save(user);

        Notification notification = notificationsRepository.findById(NotificationType.WELCOME.getTemplateId()).orElseThrow();
        String title = notification.getTitle();
        String message = notification.getMessage();

        NotificationHistories nh1 = createNotification(user, title, message, notification);
        NotificationHistories nh2 = createNotification(user, title, message, notification);

        notificationHistoriesRepository.saveAll(List.of(nh1, nh2));

        // when // then
        Assertions.assertThat(notificationHistoriesRepository.findAllByUser_Id(user.getId()))
                .hasSize(2)
                .extracting(NotificationHistories::getMessage)
                .containsExactlyInAnyOrder(message, message);
    }


    @DisplayName("사용자는 본인의 알림을 개별로 삭제할 수 있다.")
    @Test
    void deleteByUser_IdAndId() {
        // given
        User user = createUser("qpqp@naver.com", "테스트용1", "1004", "01012345678", Role.USER, 10000000L, LocalDateTime.now());
        userRepository.save(user);

        Notification notification = notificationsRepository.findById(NotificationType.WELCOME.getTemplateId()).orElseThrow();
        String title = notification.getTitle();
        String message = notification.getMessage();

        NotificationHistories nh1 = createNotification(user, title, message, notification);
        notificationHistoriesRepository.save(nh1);
        // when
        notificationHistoriesRepository.deleteByUser_IdAndId(user.getId(), nh1.getId());

        // then
        Assertions.assertThat(notificationHistoriesRepository.findAllByUser_Id(user.getId()))
                .hasSize(0);

    }

    @DisplayName("사용자는 읽지 않은 알림의 개수를 확인할 수 있다.")
    @Test
    void countByUser_IdAndIsReadFalse(){
        // given
        User user = createUser("qpqp@naver.com", "테스트용1", "1004", "01012345678", Role.USER, 10000000L, LocalDateTime.now());
        userRepository.save(user);

        Notification welcomeNotification = notificationsRepository.findById(NotificationType.WELCOME.getTemplateId()).orElseThrow();
        Notification cartNotification = notificationsRepository.findById(NotificationType.CART_LOW.getTemplateId()).orElseThrow();
        String welcomeTitle = welcomeNotification.getTitle();
        String welcomeMessage = welcomeNotification.getMessage();

        String cartTitle = cartNotification.getTitle();
        String cartMessage = cartNotification.getMessage();

        NotificationHistories nh1 = createNotification(user, welcomeTitle, welcomeMessage, welcomeNotification);
        NotificationHistories nh2 = createNotification(user, cartTitle, cartMessage, cartNotification);
        notificationHistoriesRepository.saveAll(List.of(nh1, nh2));

        // when // then

        Assertions.assertThat(notificationHistoriesRepository.countByUser_IdAndIsReadFalse(user.getId()))
                .isEqualTo(2);
    }

    @DisplayName("사용자는 읽지 않은 하나의 알림을 읽음 처리한다.")
    @Test
    void markAsRead(){
        // given
        User user = createUser("qpqp@naver.com", "테스트용1", "1004", "01012345678", Role.USER, 10000000L, LocalDateTime.now());
        userRepository.save(user);

        Notification welcomeNotification = notificationsRepository.findById(NotificationType.WELCOME.getTemplateId()).orElseThrow();

        String welcomeTitle = welcomeNotification.getTitle();
        String welcomeMessage = welcomeNotification.getMessage();

        NotificationHistories nh1 = createNotification(user, welcomeTitle, welcomeMessage, welcomeNotification);
        notificationHistoriesRepository.save(nh1);

        // when
        notificationHistoriesRepository.findByIsReadFalse(nh1.getId());

        // then
        Assertions.assertThat(notificationHistoriesRepository.findById(nh1.getId()))
                .get()
                .extracting("isRead").isEqualTo(true);
    }

    @DisplayName("사용자는 읽지 않은 알림을 모두 읽음 처리한다.")
    @Test
    void findAllByUser_IdAndIsReadFalse(){
        // given
        User user = createUser("qpqp@naver.com", "테스트용1", "1004", "01012345678", Role.USER, 10000000L, LocalDateTime.now());
        userRepository.save(user);

        Notification welcomeNotification = notificationsRepository.findById(NotificationType.WELCOME.getTemplateId()).orElseThrow();
        Notification cartNotification = notificationsRepository.findById(NotificationType.CART_LOW.getTemplateId()).orElseThrow();
        String welcomeTitle = welcomeNotification.getTitle();
        String welcomeMessage = welcomeNotification.getMessage();

        String cartTitle = cartNotification.getTitle();
        String cartMessage = cartNotification.getMessage();

        NotificationHistories nh1 = createNotification(user, welcomeTitle, welcomeMessage, welcomeNotification);
        NotificationHistories nh2 = createNotification(user, cartTitle, cartMessage, cartNotification);
        notificationHistoriesRepository.saveAll(List.of(nh1, nh2));
        // when
        notificationHistoriesRepository.findAllByUser_IdAndIsReadFalse(user.getId());

        // then
        Assertions.assertThat(notificationHistoriesRepository.countByUser_IdAndIsReadFalse(user.getId()))
                .isEqualTo(0);
    }

    private User createUser(String email, String name, String password, String mobileNumber, Role role, long salary, LocalDateTime now) {
        return User.builder()
                .email(email)
                .name(name)
                .password(password)
                .mobileNumber(mobileNumber)
                .role(role)
                .salary(salary)
                .createdAt(now)
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