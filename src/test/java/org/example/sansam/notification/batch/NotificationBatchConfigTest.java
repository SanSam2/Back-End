package org.example.sansam.notification.batch;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.example.sansam.notification.domain.Notification;
import org.example.sansam.notification.domain.NotificationHistories;
import org.example.sansam.notification.domain.NotificationType;
import org.example.sansam.notification.repository.NotificationHistoriesRepository;
import org.example.sansam.notification.repository.NotificationsRepository;
import org.example.sansam.order.domain.Order;
import org.example.sansam.order.repository.OrderRepository;
import org.example.sansam.status.domain.Status;
import org.example.sansam.status.repository.StatusRepository;
import org.example.sansam.user.domain.Role;
import org.example.sansam.user.domain.User;
import org.example.sansam.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;
import static org.example.sansam.status.domain.StatusEnum.ORDER_PAID;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@SpringBatchTest
class NotificationBatchConfigTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private NotificationHistoriesRepository notificationHistoriesRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private StatusRepository statusRepository;

    @Autowired
    private Job deleteExpiredNotificationsJob;

    @Autowired
    private Job reviewRequestJob;

    @Autowired
    private NotificationsRepository notificationsRepository;

    @Autowired
    private UserRepository userRepository;

    @PersistenceContext
    private EntityManager em;

    private User createUser() {
        return User.builder()
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

    @DisplayName("만료 알림 삭제 배치 실행 시 expiredAt 지난 알림이 삭제된다")
    @Test
    void deleteExpiredNotificationsJob_when_expired_notification_exists_then_delete_expired_notification() throws Exception {
        // given

        User user = createUser();
        userRepository.save(user);

        Notification notification = Notification.builder()
                .title("만료 알림")
                .message("이미 만료된 알림")
                .build();
        notificationsRepository.save(notification);

        NotificationHistories expired = NotificationHistories.builder()
                .user(user)
                .notification(notification)
                .title(notification.getTitle())
                .message(notification.getMessage())
                .createdAt(LocalDateTime.now().minusDays(10))
                .expiredAt(LocalDateTime.now().minusDays(3)) // 어제 만료
                .isRead(false)
                .build();
        notificationHistoriesRepository.save(expired);

        // when
        JobExecution execution = jobLauncherTestUtils.getJobLauncher()
                .run(deleteExpiredNotificationsJob,
                        new JobParametersBuilder().addLong("time", System.currentTimeMillis()).toJobParameters());

        // then
        assertThat(execution.getExitStatus().getExitCode()).isEqualTo("COMPLETED");
        assertThat(notificationHistoriesRepository.findAll()).isEmpty();
    }

//    @DisplayName("리뷰 요청 Step 실행 시, 배송 완료 후 하루 지난 주문 중 리뷰 미작성 건에 이벤트가 발행된다")
//    @Test
//    void reviewRequestJob_when_review_request_exists_then_send_notification() throws Exception {
//        // given
//        jobLauncherTestUtils.setJob(reviewRequestJob);
//
//        User user = createUser();
//        userRepository.save(user);
//
//        Status orderPaid = statusRepository.findByStatusName(ORDER_PAID);
//
//        Order order = Order.create(
//                user,
//                "운동화 주문",
//                "ORDER-12345",
//                orderPaid,
//                12000L,
//                LocalDateTime.now().minusDays(3));
//        orderRepository.save(order);
//
//        // when
//        JobExecution execution = jobLauncherTestUtils.launchStep("reviewRequestStep");
//
//        // then
//        assertThat(execution.getExitStatus().getExitCode()).isEqualTo("COMPLETED");
//    }
}