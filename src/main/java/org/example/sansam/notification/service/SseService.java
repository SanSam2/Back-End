package org.example.sansam.notification.service;

import org.example.sansam.notification.dto.NotificationResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class SseService {

    public SseEmitter connect(Integer userId) throws Exception {

        return new SseEmitter();
    }


    public void send(Long userId, NotificationResponseDTO notificationResponseDTO) {

    }
}
