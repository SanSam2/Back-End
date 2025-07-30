package org.example.sansam.notification.service;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.example.sansam.notification.dto.EmailDTO;
import org.example.sansam.payment.domain.Payment;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Service;

@Service
@Builder
@RequiredArgsConstructor
public class EmailService {

//    private final JavaMailSender javaMailSender;

    public void sendWelcomeEmail(String toEmail, String username) throws MessagingException {
        EmailDTO dto = EmailDTO.builder()
                .to(toEmail)
                .subject("회원 가입을 축하드립니다!")
                .content(" \"<h1>환영합니다, \" + username + \"님 \uD83C\uDF89</h1>\" +\n" +
                        "                \"<p>OOO 서비스를 시작해보세요!</p>\"")
                .build();

//        sendMail(dto);
    }

    // 결제 완료 시 호출 되는 메서드 / 결제 정보 받아서 완료 내역 이메일 발송
    public void sendPaymentCompletedEmail() throws MessagingException {
        Payment payment = new Payment();

    }
    // 결제 취소 시 호출 되는 메서드 / 결제 취소 정보 받아서 취소 내역 이메일 발송
    public void sendPaymentCanceledEmail(String toEmail, String username) throws MessagingException {

    }

}
