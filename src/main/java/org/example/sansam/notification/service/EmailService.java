package org.example.sansam.notification.service;

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.model.*;
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

    /**
     * Sends a welcome email to the specified user using a Thymeleaf template.
     *
     * @param user the user to receive the welcome email
     * @throws RuntimeException if the email fails to send
     */
    public void sendWelcomeEmail(User user) {
        try {
            MimeMessage message = createWelcomeMessage(user);
            if (user.getEmailAgree())javaMailSender.send(message);
        } catch (MessagingException | jakarta.mail.MessagingException e) {
            log.error("회원 가입 이메일 전송 실패 - {}", user.getEmail());
            throw new RuntimeException(e);
        }
    }

    private MimeMessage createWelcomeMessage(User user) throws MessagingException, jakarta.mail.MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

        helper.setTo(user.getEmail());
        helper.setSubject("[Or de Firenz 회원가입을 축하드립니다.]");

        Context context = new Context();
        context.setVariable("name", user.getName());

        String html = templateEngine.process("email/signup", context);
        helper.setText(html, true);

        return message;
    }

    /**
     * Sends a payment completion email to the specified user with order details and final price.
     *
     * @param user the recipient user
     * @param orderName the name of the completed order
     * @param finalPrice the final price paid for the order
     * @throws RuntimeException if sending the email fails
     */
    public void sendPaymentCompletedEmail(User user, String orderName, Long finalPrice) {
        try {
            MimeMessage message = createPaymentCompletedMessage(user, orderName, finalPrice);
            if (user.getEmailAgree()) javaMailSender.send(message);
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

    /**
     * Sends a payment cancellation email to the specified user with order and refund details.
     *
     * @param user the recipient of the email
     * @param orderName the name of the canceled order
     * @param refundPrice the amount refunded to the user
     * @throws RuntimeException if sending the email fails
     */
    public void sendPaymentCanceledMessage(User user, String orderName, Long refundPrice) {
        try {
            MimeMessage message = createPaymentCanceledMessage(user, orderName, refundPrice);
            if (user.getEmailAgree())javaMailSender.send(message);
        } catch (Exception e) {
            log.error("결제 취소 완료 이메일 전송 실패 - {}", user.getEmail());
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a MIME email message notifying the user of a payment cancellation, including order details and refund amount.
     *
     * @param user the recipient user
     * @param orderName the name of the canceled order
     * @param refundPrice the amount refunded to the user
     * @return a MimeMessage containing the payment cancellation notification
     * @throws MessagingException if an error occurs while constructing the message
     * @throws jakarta.mail.MessagingException if a Jakarta Mail-specific error occurs
     */
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
        System.out.println("Rendered HTML: " + html);
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
