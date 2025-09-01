package org.example.sansam.notification.service;

import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.assertj.core.api.Assertions;
import org.example.sansam.user.domain.Role;
import org.example.sansam.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = EmailService.class)
@ActiveProfiles("test")
class EmailServiceTest {

    @Autowired
    private EmailService emailService;

    @MockitoBean
    private JavaMailSender mailSender;

    @MockitoBean
    private SpringTemplateEngine templateEngine;


    @DisplayName("회원가입 환영 메일: 이메일 동의한 유저면 전송된다")
    @Test
    void sendWelcomeEmailWithAgreeUser() throws Exception {
        // given
        User user = agreeUser();

        // JavaMailSender가 사용할 빈 메시지 준비
        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));

        // 템플릿 렌더링 결과 스텁
        when(templateEngine.process(eq("email/signup"), any(Context.class)))
                .thenReturn("<html>WELCOME " + user.getName() + "</html>");

        // when
        emailService.sendWelcomeEmail(user);
        // then

        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender, times(1)).send(captor.capture());

        MimeMessage sent = captor.getValue();
        Assertions.assertThat(sent.getAllRecipients()[0].toString()).isEqualTo(user.getEmail());
        Assertions.assertThat(sent.getSubject()).isEqualTo("[Or de Firenz 회원가입을 축하드립니다.]");

        // MimeMessageHelper(true)로 만들었으니 multipart일 수 있음 → 내용 추출
        Object content = sent.getContent();
        String body = extractBodyAsString(content);
        Assertions.assertThat(body).contains("WELCOME");
        Assertions.assertThat(body).contains(user.getName());
    }

    @DisplayName("회원가입 환영 메일: 이메일 동의하지 않은 유저는 전송이 되지 않는다")
    @Test
    void sendWelcomeEmailWithDisagreeUser(){
        // given
        User user = disagreeUser();

        // JavaMailSender가 사용할 빈 메시지 준비
        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));

        // 템플릿 렌더링 결과 스텁
        when(templateEngine.process(eq("email/signup"), any(Context.class)))
                .thenReturn("<html>WELCOME " + user.getName() + "</html>");

        // when
        emailService.sendWelcomeEmail(user);
        // then
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @DisplayName("결제 완료 메일 : 이메일 동의한 유저라면 결제 완료 메일이 전송된다.")
    @Test
    void sendPaymentCompletedEmail() {
        // given
        User user = agreeUser();
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("email/payment-completed"), any()))
                .thenReturn("<html>결제 완료</html>");

        // when
        emailService.sendPaymentCompletedEmail(user, "테스트 상품",1000L);

        // then
        verify(mailSender, times(1)).send(mimeMessage);
    }

    @DisplayName("결제 완료 메일 : 이메일 동의하지 않은 유저라면 결제 완료 메일이 전송되지 않는다.")
    @Test
    void sendPaymentCompletedEmail_when_disagree_user(){
        // given
        User user = disagreeUser();
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("email/payment-completed"), any()))
                .thenReturn("<html>결제 완료</html>");

        // when
        emailService.sendPaymentCompletedEmail(user, "테스트 상품",1000L);

        // then
        verify(mailSender, never()).send(mimeMessage);
    }

    @DisplayName("결제 취소 메일 : 이메일 동의한 유저라면 결제 취소 메일이 전송된다.")
    @Test
    void sendPaymentCanceledMessage() {
        // given
        User user = agreeUser();
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("email/payment-canceled"), any()))
                .thenReturn("<html>결제 취소</html>");

        // when
        emailService.sendPaymentCanceledMessage(user, "테스트상품", 5000L);

        // then
        verify(mailSender, times(1)).send(mimeMessage);
    }

    @DisplayName("결제 취소 메일 : 이메일 동의하지 않은 유저라면 결제 취소 메일이 전송되지 않는다.")
    @Test
    void sendPaymentCanceledMessage_when_disagree_user(){
        // given
        User user = disagreeUser();
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("email/payment-canceled"), any()))
                .thenReturn("<html>결제 취소</html>");
        // when
        emailService.sendPaymentCanceledMessage(user, "테스트상품", 5000L);
        // then
        verify(mailSender, never()).send(mimeMessage);
    }

    private User agreeUser() {
        return User.builder()
                .email("dvbf@naver.com")
                .name("테스트1")
                .password("1004")
                .mobileNumber("01012345678")
                .role(Role.USER)
                .salary(10000000L)
                .createdAt(LocalDateTime.now())
                .activated(true)
                .emailAgree(true)
                .build();
    }

    private User disagreeUser() {
        return User.builder()
                .email("dvbf@naver.com")
                .name("테스트1")
                .password("1004")
                .mobileNumber("01012345678")
                .role(Role.USER)
                .salary(10000000L)
                .createdAt(LocalDateTime.now())
                .activated(true)
                .emailAgree(false)
                .build();
    }

    private String extractBodyAsString(Object content) throws Exception {
        if (content instanceof String s) {
            return s;
        }
        if (content instanceof jakarta.mail.Multipart mp) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mp.getCount(); i++) {
                var part = mp.getBodyPart(i);
                Object pContent = part.getContent();
                // text/html 우선
                if (part.getContentType().toLowerCase().contains("text/html")) {
                    if (pContent instanceof String html) {
                        sb.append(html);
                    }
                } else if (pContent instanceof String plain) {
                    sb.append(plain);
                } else if (pContent instanceof InputStream is) {
                    sb.append(new String(is.readAllBytes()));
                } else if (pContent instanceof jakarta.mail.Multipart nested) {
                    // nested multipart까지 재귀 탐색
                    sb.append(extractBodyAsString(nested));
                }
            }
            return sb.toString();
        }
        return content == null ? "" : content.toString();
    }

}