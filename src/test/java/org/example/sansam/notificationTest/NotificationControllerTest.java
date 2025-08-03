package org.example.sansam.notificationTest;

import org.example.sansam.notification.controller.NotificationController;
import org.example.sansam.notification.dto.NotificationDTO;
import org.example.sansam.notification.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
public class NotificationControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    @Test
    void getNotifications_shouldReturnList() throws Exception {
        // given
        NotificationDTO dto = NotificationDTO.builder()
                .id(1L)
                .title("알림입니다")
                .message("내용입니다")
                .build();
        given(notificationService.getNotificationHistories(1L))
                .willReturn(List.of(dto));

        // when + then
        mockMvc.perform(get("/api/notifications/list/{userId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("알림입니다"))
                .andExpect(jsonPath("$[0].message").value("내용입니다"))
                .andDo(print());
    }
    @Test
    void getUnreadNotificationCount_shouldReturnCount() throws Exception {
        given(notificationService.getUnreadNotificationCount(2L)).willReturn(3L);

        mockMvc.perform(get("/api/notifications/unread-count/2"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("3"));
    }

    @Test
    void markAsRead_shouldReturnOk() throws Exception {
        MvcResult result = mockMvc.perform(patch("/api/notifications/read/10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        verify(notificationService).markAsRead(10L);
        System.out.println(result.getResponse().getStatus());
    }

    @Test
    void markAllAsRead_shouldReturnOk() throws Exception {
        MvcResult result = mockMvc.perform(patch("/api/notifications/read-all/2"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        verify(notificationService).markAllAsRead(2L);
        System.out.println("응답: " + result.getResponse().getStatus());
    }
}
