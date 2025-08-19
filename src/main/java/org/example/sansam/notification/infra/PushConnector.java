package org.example.sansam.notification.infra;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface PushConnector {

    SseEmitter connect(Long userId);

}
