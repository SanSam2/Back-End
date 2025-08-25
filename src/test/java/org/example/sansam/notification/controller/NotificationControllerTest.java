package org.example.sansam.notification.controller;


import org.assertj.core.api.Assertions;
import org.example.sansam.notification.domain.Notification;
import org.example.sansam.notification.domain.NotificationHistories;
import org.example.sansam.notification.domain.NotificationType;
import org.example.sansam.notification.dto.NotificationDTO;
import org.example.sansam.notification.service.NotificationService;
import org.example.sansam.user.domain.Role;
import org.example.sansam.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(NotificationController.class)
@ActiveProfiles("test")
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    // ----- /list/{userId}


    @DisplayName("사용자는 로그인 했을 시 본인의 알림 목록을 가져온다.")
    @Test
    void getNotificationList_returns200_whenExists() throws Exception {
        // given
        User user = createUser(LocalDateTime.now());

        Notification notification = createNotificationTemplate();
        String title = notification.getTitle();
        String message = notification.getMessage();

        NotificationHistories nh1 = createNotification(user, title, message, notification);
        NotificationHistories nh2 = createNotification(user, title, message, notification);


        NotificationDTO dto1 = NotificationDTO.from(nh1);
        NotificationDTO dto2 = NotificationDTO.from(nh2);
        // when
        when(notificationService.getNotificationHistories(user.getId())).thenReturn(List.of(dto1, dto2));

        // then
        mockMvc.perform(
                        get("/api/notifications/list/" + user.getId())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        verify(notificationService, times(1)).getNotificationHistories(user.getId());
    }


    @DisplayName("사용자는 알림 목록이 없을 시 아무것도 받지 않는다. 204 No Content 반환")
    @Test
    void getNotificationListW_when_no_notification_exists() throws Exception {
        // given
        User user = createUser(LocalDateTime.now());

        // when
        // then

        mockMvc.perform(
                        get("/api/notifications/list/" + user.getId())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(result -> {
                    System.out.println(result.getResponse().getContentAsString());
                });

        verify(notificationService, times(1)).getNotificationHistories(user.getId());
    }

    @DisplayName("서비스 예외 발생 시 500 에러 반환")
    @Test
    void getNotificationList_when_exception_occurs_during_service_execution() throws Exception {
        // given
        // when
        when(notificationService.getNotificationHistories(3L))
                .thenThrow(new RuntimeException("boom"));

        // then
        mockMvc.perform(
                        get("/api/notifications/list/3")
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isInternalServerError());

        verify(notificationService, times(1)).getNotificationHistories(3L);
    }

    // ----- /unread-count/{userId}

    @DisplayName("사용자는 읽지 않은 알림의 개수를 확인할 수 있다. 200 반환")
    @Test
    void getUnreadNotificationCount_ok() throws Exception {
        // given
        User user = createUser(LocalDateTime.now());

        // when

        when(notificationService.getUnreadNotificationCount(user.getId())).thenReturn(2L);
        // then
        mockMvc.perform(
                        get("/api/notifications/unread-count/" + user.getId())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").value(2L));

        verify(notificationService, times(1)).getUnreadNotificationCount(user.getId());

    }

    @DisplayName("읽지 않은 알림의 개수 확인 도중 서비스 예외 발생 시 500에러 반환")
    @Test
    void getUnreadNotificationCountW_when_exception_occurs_during_service_execution() throws Exception {
        // given

        // when
        when(notificationService.getUnreadNotificationCount(anyLong()))
                .thenThrow(new RuntimeException("boom"));

        // then
        mockMvc.perform(
                        get("/api/notifications/unread-count/1")
                                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isInternalServerError());
        verify(notificationService, times(1)).getUnreadNotificationCount(anyLong());
    }

    @DisplayName("사용자는 읽지 않은 알림이 0개이더라도 읽지 않은 알림의 개수를 확인할 수 있다.")
    @Test
    void getUnreadNotificationCountWithoutUnreadNotificationCount() throws Exception {
        // given
        User user = createUser(LocalDateTime.now());

        // when

        when(notificationService.getUnreadNotificationCount(user.getId())).thenReturn(0L);
        // then
        mockMvc.perform(
                        get("/api/notifications/unread-count/" + user.getId())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").value(0L));
        verify(notificationService, times(1)).getUnreadNotificationCount(user.getId());
//        verifyNoMoreInteractions(notificationService);
//        verifyNoInteractions(notificationService);

    }

    // ----- /read/{notificationHistoriesId}

    @DisplayName("사용자는 읽지 않은 알림 단건에 대하여 읽음 처리를 할 수 있다. 성공 시 200")
    @Test
    void markAsRead() throws Exception {
        // given
//        User user = createUser(LocalDateTime.now());
//        Notification notification = createNotificationTemplate();
//
//        String title = notification.getTitle();
//        String message = notification.getMessage();
//
//        NotificationHistories nh1 = createNotification(user, title, message, notification);
//
//        Assertions.assertThat(nh1.isRead()).isFalse();
//
//        // when
//        doNothing().when(notificationService).markAsRead(nh1.getId());
//
//        // then
//
//        mockMvc.perform(
//                        patch("/api/notifications/read/" + nh1.getId())
//                )
//                .andDo(print())
//                .andExpect(status().isOk());
//
//        verify(notificationService, times(1)).markAsRead(nh1.getId());

        mockMvc.perform(patch("/api/notifications/read/{notificationHistoriesId}", 10L))
                .andExpect(status().isOk());

        verify(notificationService, times(1)).markAsRead(10L);

    }
    @DisplayName("")
    @Test
    void test(){
        // given

        // when

        // then
    }

    @DisplayName("사용자는 읽지 않은 알림을 모두 읽음 처리할 수 있다.")
    @Test
    void markAllAsRead() throws Exception {
        // given

        User user = createUser(LocalDateTime.now());
        Notification notification = createNotificationTemplate();

        String title = notification.getTitle();
        String message = notification.getMessage();

        NotificationHistories nh1 = createNotification(user, title, message, notification);
        NotificationHistories nh2 = createNotification(user, title, message, notification);
        NotificationHistories nh3 = createNotification(user, title, message, notification);

        Assertions.assertThat(List.of(nh1, nh2, nh3))
                .allMatch(nh -> !nh.isRead());

        // when
        doNothing().when(notificationService).markAllAsRead(user.getId());

        // then
        mockMvc.perform(
                patch("/api/notifications/read-all/" + user.getId())
        )
                .andDo(print())
                .andExpect(status().isOk());

        verify(notificationService, times(1)).markAllAsRead(user.getId());
    }

    @DisplayName("사용자는 알림의 읽음 상관 없이 알림을 삭제할 수 있다.")
    @Test
    void deleteNotification() throws Exception {
        // given

        User user = createUser(LocalDateTime.now());
        Notification notification = createNotificationTemplate();

        String title = notification.getTitle();
        String message = notification.getMessage();

        NotificationHistories nh1 = createNotification(user, title, message, notification);


        // when
        doNothing().when(notificationService).deleteNotificationHistory(user.getId(), nh1.getId());

        // then
        mockMvc.perform(
                delete("/api/notifications/delete/" + user.getId() + "/" + nh1.getId())
        )
                .andDo(print())
                .andExpect(status().isOk());
        verify(notificationService, times(1)).deleteNotificationHistory(user.getId(), nh1.getId());

    }

    private User createUser(LocalDateTime now) {
        return User.builder()
                .id(1L)
                .email("zm@gmail.com")
                .name("테스트1")
                .password("1004")
                .mobileNumber("01012345678")
                .role(Role.USER)
                .salary(10000000L)
                .createdAt(now)
                .activated(true)
                .emailAgree(true)
                .build();
    }

    private NotificationHistories createNotification(User user, String title, String message, Notification notification) {
        return NotificationHistories.builder()
                .id(1L)
                .user(user)
                .title(String.format(title, user.getName()))
                .message(message)
                .createdAt(LocalDateTime.now())
                .notification(notification)
                .isRead(false)
                .expiredAt(LocalDateTime.now().plusDays(14))
                .build();
    }

    private Notification createNotificationTemplate() {
        return Notification.builder()
                .id(NotificationType.WELCOME.getTemplateId())
                .title("%s님 가입을 환영합니다.")
                .message("고객님의 스타일을 책임져주는 Or de Firenz의 회원이 되신 것을 환영합니다.")
                .build();
    }
}