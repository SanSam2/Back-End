package org.example.sansam.notification.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.sansam.user.domain.User;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine templateEngine;
//    private final SimpleEmailServiceJavaMailSender simpleEmailServiceJavaMailSender;
    // aws ses 전용 -> ses로 전환할 때 사용

    public void sendWelcomeEmail(User user) {
        try {
            MimeMessage message = createWelcomeMessage(user);
            javaMailSender.send(message);
            log.info("회원 가입 이메일 전송 완료 - {}", user.getEmail());
        } catch (MessagingException | jakarta.mail.MessagingException e) {
            log.error("회원 가입 이메일 전송 실패 - {}", user.getEmail());
            throw new RuntimeException(e);
        }
    }

    private MimeMessage createWelcomeMessage(User user) throws MessagingException, jakarta.mail.MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(user.getEmail());
        helper.setSubject("[산삼몰 회원가입을 축하드립니다!!!]");

        Context context = new Context();
        context.setVariable("name", user.getName());

        String html = templateEngine.process("email/signup", context);
        helper.setText(html, true);

        return message;
    }

    public void sendPaymentCompletedEmail(User user, String orderName, Long finalPrice) {
        try {
            MimeMessage message = createPaymentCompletedMessage(user, orderName, finalPrice);
            javaMailSender.send(message);
            log.info("결제 완료 이메일 전송 완료 - {}", user.getEmail());
        } catch (Exception e){
            log.error("결제 완료 이메일 전송 실패 - {}", user.getEmail());
            throw new RuntimeException(e);
        }
    }

    private MimeMessage createPaymentCompletedMessage(User user, String orderName, Long finalPrice) throws MessagingException, jakarta.mail.MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(user.getEmail());
        helper.setSubject("[산삼몰 결제 완료 안내]");

        Context context = new Context();
        context.setVariable("name", user.getName());
        context.setVariable("orderName", orderName);
        context.setVariable("finalPrice", finalPrice);

        String html = templateEngine.process("email/payment-completed", context);
        helper.setText(html, true);

        return message;
    }

    public void sendPaymentCanceledMessage(User user, String orderName, Long refundPrice) {
        try {
            MimeMessage message = createPaymentCanceledMessage(user, orderName, refundPrice);
            javaMailSender.send(message);
            log.info("결제 취소 완료 이메일 전송 완료 - {}", user.getEmail());
        } catch (Exception e){
            log.error("결제 취소 완료 이메일 전송 실패 - {}", user.getEmail());
            throw new RuntimeException(e);
        }
    }

    private MimeMessage createPaymentCanceledMessage(User user, String orderName, Long refundPrice) throws MessagingException, jakarta.mail.MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(user.getEmail());
        helper.setSubject("[산삼몰 결제 취소 완료 안내]");

        Context context = new Context();
        context.setVariable("name", user.getName());
        context.setVariable("orderName", orderName);
        context.setVariable("refundPrice", refundPrice);

        String html = templateEngine.process("email/payment-canceled", context);
        helper.setText(html, true);
        System.out.println("Rendered HTML: " + html);
        return message;
    }
}
