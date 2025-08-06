package org.example.sansam.notificationTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.sansam.notification.domain.Notification;
import org.example.sansam.notification.domain.NotificationHistories;
import org.example.sansam.notification.dto.NotificationDTO;
import org.example.sansam.notification.exception.CustomException;
import org.example.sansam.notification.exception.ErrorCode;
import org.example.sansam.notification.repository.NotificationHistoriesRepository;
import org.example.sansam.notification.repository.NotificationsRepository;
import org.example.sansam.notification.service.NotificationService;
import org.example.sansam.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.when;

class NotificationServiceTest {

    @Mock
    private NotificationsRepository notificationsRepository;

    @Mock
    private NotificationHistoriesRepository notificationHistoriesRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private NotificationService notificationService;

    private User user;
    private Notification notificationTemplate;
    private NotificationHistories notificationHistory;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = User.builder()
                .id(2L)
                .name("유저2")
                .build();

        notificationTemplate = Notification.builder()
                .id(1L)
                .title("%s님 가입을 환영합니다.")
                .message("고객님의 스타일을 책임져주는 LUNARE의 회원이 되신 것을 환영합니다.")
                .build();

        notificationHistory = NotificationHistories.builder()
                .id(1L)
                .user(user)
                .notification(notificationTemplate)
                .title("유저2님 가입을 환영합니다.")
                .message("고객님의 스타일을 책임져주는 LUNARE의 회원이 되신 것을 환영합니다.")
                .createdAt(Timestamp.valueOf(LocalDateTime.now()))
                .expiredAt(Timestamp.valueOf(LocalDateTime.now().plusDays(14)))
                .isRead(false)
                .build();
    }

    @Test
    void getNotificationHistories_shouldReturnDTOList() {
        when(notificationHistoriesRepository.findAllByUser_Id(user.getId()))
                .thenReturn(List.of(notificationHistory));

        List<NotificationDTO> result = notificationService.getNotificationHistories(user.getId());

        result.forEach(System.out::println);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo(notificationHistory.getTitle());
    }

    @Test
    void getUnreadNotificationCount_shouldReturnCount() {
        when(notificationHistoriesRepository.countByUser_IdAndIsReadFalse(user.getId())).thenReturn(3L);

        Long result = notificationService.getUnreadNotificationCount(user.getId());
        System.out.println(result);

        assertThat(result).isEqualTo(3L);
    }

    @Test
    void markAsRead_shouldUpdateReadStatus() {
        when(notificationHistoriesRepository.findById(1L)).thenReturn(Optional.of(notificationHistory));

        notificationService.markAsRead(1L);
        System.out.println("isRead: " + notificationHistory.isRead());
        assertThat(notificationHistory.isRead()).isTrue();
    }

    @Test
    void markAsRead_shouldThrowExceptionIfNotFound() {
        when(notificationHistoriesRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.markAsRead(1L))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.EMITTER_NOT_FOUND.getMessage());
    }

    @Test
    void markAllAsRead_shouldMarkAllUnreadNotifications() {
        NotificationHistories unread1 = NotificationHistories.builder().id(10L).user(user).isRead(false).build();
        NotificationHistories unread2 = NotificationHistories.builder().id(11L).user(user).isRead(false).build();

        when(notificationHistoriesRepository.findAllByUser_IdAndIsReadFalse(user.getId()))
                .thenReturn(List.of(unread1, unread2));

        notificationService.markAllAsRead(user.getId());

        assertThat(unread1.isRead()).isTrue();
        assertThat(unread2.isRead()).isTrue();

        System.out.println("unread1.isRead = " + unread1.isRead());
        System.out.println("unread2.isRead = " + unread2.isRead());
    }
}

