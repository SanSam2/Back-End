package org.example.sansam.notification.eventListener;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.sansam.notification.event.UserWelcomeEmailEvent;
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
        emailService.sendWelcomeEmail(event.getUser());
    }
}
