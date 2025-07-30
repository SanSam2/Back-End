package org.example.sansam.notification.event;


import lombok.RequiredArgsConstructor;
import org.example.sansam.notification.service.EmailService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailEventListener {
    private final EmailService emailService;

    @EventListener
    public void handelEmailEvent(EmailEvent event) {

        try {
//            emailService.sendWelcomeEmail(event.getToEmail() ,event.getTitle(), event.getBody());
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
