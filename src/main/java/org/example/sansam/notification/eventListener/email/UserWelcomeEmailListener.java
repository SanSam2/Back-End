package org.example.sansam.notification.eventListener.email;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.sansam.notification.event.email.UserWelcomeEmailEvent;
import org.example.sansam.notification.service.EmailService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserWelcomeEmailListener {
    private final EmailService emailService;

    @Async
    @EventListener
    public void handleUserSignUp(UserWelcomeEmailEvent event) {
        try {
            emailService.sendWelcomeEmail(event.getUser());
        } catch (Exception e) {
            log.error("회원가입 환영 메일 전송 실패 - userId={}", event.getUser() != null ? event.getUser().getId() : "null", e);
        }
    }
}
