package org.example.sansam.global.config;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.AddressException;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.MailSendException;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.interceptor.RetryInterceptorBuilder;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;


@Configuration
@EnableRetry
@Log4j2
public class RetryMailConfig {

    @Bean
    public RetryOperationsInterceptor emailRetryInterceptor() {
        return RetryInterceptorBuilder.stateless()
                .maxAttempts(3)
                .backOffOptions(2000, 2.0, 10000)  // 2s, 4s, 8s (최대 10s)
                .recoverer((args, cause) -> {
                    String targetEmail = (args != null && args.length > 0 && args[0] != null)
                            ? args[0].toString()
                            : "unknown";

                    log.error("[EMAIL-FAIL] 최종 실패 - target={}, 이유={}", targetEmail, cause.getMessage(), cause);
                    return null; // 반드시 리턴 필요
                })
                .build();
    }
}
