package org.example.sansam.notification.service;


import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.sansam.user.domain.User;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.messaging.MessagingException;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender javaMailSender = new JavaMailSenderImpl();

    private final SpringTemplateEngine templateEngine;

    @Retryable(
            interceptor = "emailRetryInterceptor",
            include = {MailSendException.class, MessagingException.class},
            exclude = {AddressException.class}
    )
    public void sendWelcomeEmail(User user) {
        try {
            MimeMessage message = createWelcomeMessage(user);
            if (user.getEmailAgree())javaMailSender.send(message);
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
        helper.setSubject("[Or de Firenz 회원가입을 축하드립니다.]");

        Context context = new Context();
        context.setVariable("name", user.getName());

        String html = templateEngine.process("email/signup", context);
        helper.setText(html, true);

        return message;
    }

    @Retryable(
            interceptor = "emailRetryInterceptor",
            include = {MailSendException.class, MessagingException.class},
            exclude = {AddressException.class}
    )
    public void sendPaymentCompletedEmail(User user, String orderName, Long finalPrice) {
        try {
            MimeMessage message = createPaymentCompletedMessage(user, orderName, finalPrice);
            if (user.getEmailAgree()) javaMailSender.send(message);
            log.info("결제 완료 이메일 전송 완료 - {}", user.getEmail());
        } catch (Exception e) {
            log.error("결제 완료 이메일 전송 실패 - {}", user.getEmail());
            throw new RuntimeException(e);
        }
    }

    private MimeMessage createPaymentCompletedMessage(User user, String orderName, Long finalPrice) throws MessagingException, jakarta.mail.MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(user.getEmail());
        helper.setSubject("[Or de Firenz 결제 완료 안내]");

        Context context = new Context();
        context.setVariable("name", user.getName());
        context.setVariable("orderName", orderName);
        context.setVariable("finalPrice", finalPrice);

        String html = templateEngine.process("email/payment-completed", context);
        helper.setText(html, true);

        return message;
    }

    @Retryable(
            interceptor = "emailRetryInterceptor",
            include = {MailSendException.class, MessagingException.class},
            exclude = {AddressException.class}
    )
    public void sendPaymentCanceledMessage(User user, String orderName, Long refundPrice) {
        try {
            MimeMessage message = createPaymentCanceledMessage(user, orderName, refundPrice);
            if (user.getEmailAgree())javaMailSender.send(message);
            log.info("결제 취소 완료 이메일 전송 완료 - {}", user.getEmail());
        } catch (Exception e) {
            log.error("결제 취소 완료 이메일 전송 실패 - {}", user.getEmail());
            throw new RuntimeException(e);
        }
    }

    private MimeMessage createPaymentCanceledMessage(User user, String orderName, Long refundPrice) throws MessagingException, jakarta.mail.MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(user.getEmail());
        helper.setSubject("[Or de Firenz 결제 취소 완료 안내]");

        Context context = new Context();
        context.setVariable("name", user.getName());
        context.setVariable("orderName", orderName);
        context.setVariable("refundPrice", refundPrice);

        String html = templateEngine.process("email/payment-canceled", context);
        helper.setText(html, true);
        return message;
    }

    //    private final AmazonSimpleEmailService amazonSimpleEmailService;
//
//    private static final String SENDER = "hosang10041007@gmail.com";
//
//
//    public void sendWelcomeEmail(User user) {
//        String subject = "[Or de Firenz 회원가입을 축하드립니다.]";
//
//        Context context = new Context();
//        context.setVariable("name", user.getName());
//        String html = templateEngine.process("email/signup", context);
//
//        sendHtmlEmail(SENDER, subject, html);   // 원래는 user.getEmail()
//    }
//
//    public void sendPaymentCompletedEmail(User user, String orderName, Long finalPrice) {
//        String subject = "[Or de Firenz 결제 완료 안내]";
//
//        Context context = new Context();
//        context.setVariable("name", user.getName());
//        context.setVariable("orderName", orderName);
//        context.setVariable("finalPrice", finalPrice);
//        String html = templateEngine.process("email/payment-completed", context);
//
//        sendHtmlEmail(SENDER, subject, html);
//    }
//
//    public void sendPaymentCanceledMessage(User user, String orderName, Long refundPrice) {
//        String subject = "[Or de Firenz 결제 취소 완료 안내]";
//
//        Context context = new Context();
//        context.setVariable("name", user.getName());
//        context.setVariable("orderName", orderName);
//        context.setVariable("refundPrice", refundPrice);
//        String html = templateEngine.process("email/payment-canceled", context);
//
//        sendHtmlEmail(SENDER, subject, html);
//    }
//
//    private void sendHtmlEmail(String to, String subject, String htmlBody) {
//        try {
//            SendEmailRequest request = new SendEmailRequest()
//                    .withDestination(new Destination().withToAddresses(to))
//                    .withMessage(new Message()
//                            .withSubject(new Content().withCharset("UTF-8").withData(subject))
//                            .withBody(new Body().withHtml(new Content().withCharset("UTF-8").withData(htmlBody))))
//                    .withSource(to);
//
//            amazonSimpleEmailService.sendEmail(request);
//            log.info("SES 이메일 전송 성공 - {}", to);
//
//        } catch (Exception e) {
//            log.error("SES 이메일 전송 실패 - {}", to, e);
//            throw new RuntimeException("SES 이메일 전송 실패", e);
//        }
//    }
}
