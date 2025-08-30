package org.example.sansam.payment.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.sansam.exception.pay.CustomException;
import org.example.sansam.exception.pay.ErrorCode;
import org.example.sansam.payment.domain.PaymentMethodType;
import org.example.sansam.payment.domain.PaymentsType;
import org.example.sansam.payment.repository.PaymentsTypeRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class TossApprovalNormalizer {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private final PaymentsTypeRepository paymentsTypeRepository;
    private final ObjectMapper objectMapper;

    public Normalized normalize(Map<String, Object> resp, String paymentKey) {
        String methodKor = String.valueOf(resp.get("method"));
        PaymentMethodType methodType = PaymentMethodType.fromKorean(methodKor);

        long totalPrice = ((Number) resp.get("totalAmount")).longValue();
        long finalPrice = ((Number) resp.get("balanceAmount")).longValue();

        LocalDateTime requestedAt = toKst(resp.get("requestedAt"));
        LocalDateTime approvedAt  = toKst(resp.get("approvedAt"));

        PaymentsType paymentsType = paymentsTypeRepository.findByTypeName(methodType)
                .orElseThrow(() -> new CustomException(ErrorCode.UNSUPPORTED_PAYMENT_METHOD));


        return new Normalized(paymentKey, paymentsType, totalPrice, finalPrice, requestedAt, approvedAt);
    }

    private static LocalDateTime toKst(Object v) {
        if (v == null) return null;
        return OffsetDateTime.parse(String.valueOf(v))
                .atZoneSameInstant(KST)
                .toLocalDateTime();
    }

    public record Normalized(
            String paymentKey,
            PaymentsType paymentsType,
            long totalAmount,
            long balanceAmount,
            LocalDateTime requestedAtKst,
            LocalDateTime approvedAtKst
    ) {}
}
