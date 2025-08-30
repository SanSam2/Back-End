package org.example.sansam.payment.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.sansam.exception.pay.CustomException;
import org.example.sansam.exception.pay.ErrorCode;
import org.example.sansam.payment.domain.PaymentMethodType;
import org.example.sansam.payment.domain.PaymentsType;
import org.example.sansam.payment.repository.PaymentsTypeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TossApprovalNormalizerTest {
    @Mock
    PaymentsTypeRepository paymentsTypeRepository;

    // ObjectMapper는 현재 normalize에서 쓰이지 않지만, 생성자 주입 때문에 넣어둠
    @Mock
    ObjectMapper objectMapper;

    @InjectMocks
    TossApprovalNormalizer normalizer;

    private static Map<String, Object> resp(Object method, Object total, Object balance,
                                            Object requestedAt, Object approvedAt) {
        Map<String, Object> m = new HashMap<>();
        m.put("method", method);
        m.put("totalAmount", total);
        m.put("balanceAmount", balance);
        m.put("requestedAt", requestedAt);
        m.put("approvedAt", approvedAt);
        return m;
    }

    @Test
    void 정상적인_입력이면_KST_LDT가_동일시각이다_금액과_메서드도_올바르게_변환한다() {
        // given
        PaymentsType cardType = new PaymentsType(); // 필드 접근 안 하므로 빈 객체면 충분
        when(paymentsTypeRepository.findByTypeName(PaymentMethodType.CARD))
                .thenReturn(Optional.of(cardType));

        // +09:00 → KST로 변환 시 로컬 시각 동일
        Map<String, Object> source = resp(
                "카드",
                Integer.valueOf(12000),
                Long.valueOf(3000),
                "2025-08-24T10:00:00+09:00",
                "2025-08-24T10:05:00+09:00"
        );

        // when
        TossApprovalNormalizer.Normalized n = normalizer.normalize(source, "pay_123");

        // then
        assertEquals("pay_123", n.paymentKey());
        assertSame(cardType, n.paymentsType());
        assertEquals(12000L, n.totalAmount());
        assertEquals(3000L, n.balanceAmount());
        assertEquals(LocalDateTime.of(2025, 8, 24, 10, 0, 0), n.requestedAtKst());
        assertEquals(LocalDateTime.of(2025, 8, 24, 10, 5, 0), n.approvedAtKst());

        verify(paymentsTypeRepository).findByTypeName(PaymentMethodType.CARD);
        verifyNoMoreInteractions(paymentsTypeRepository);
    }

    @Test
    void utc입력에대해서_KST로_정상적으로_보정한다() {
        // given
        when(paymentsTypeRepository.findByTypeName(PaymentMethodType.TRANSFER))
                .thenReturn(Optional.of(new PaymentsType()));

        Map<String, Object> source = resp(
                "계좌이체",
                1000L,
                0L,
                "2025-08-24T00:00:00+00:00",
                "2025-08-24T00:01:00+00:00"
        );

        // when
        TossApprovalNormalizer.Normalized n = normalizer.normalize(source, "pay_utc");

        // then: KST는 +9h
        assertEquals(LocalDateTime.of(2025, 8, 24, 9, 0, 0), n.requestedAtKst());
        assertEquals(LocalDateTime.of(2025, 8, 24, 9, 1, 0), n.approvedAtKst());
    }

    @Test //사실상 approvedAt이 null로 온다는건, 토스가 잘못된것
    void approvedAt이_null로넘어오게되면_Null값을_유지한다() {
        // given
        when(paymentsTypeRepository.findByTypeName(PaymentMethodType.EASY_PAY))
                .thenReturn(Optional.of(new PaymentsType()));

        Map<String, Object> source = resp(
                "간편결제",
                5000,
                1000,
                "2025-08-24T12:00:00+09:00",
                null
        );

        // when
        TossApprovalNormalizer.Normalized n = normalizer.normalize(source, "pay_pending");

        // then
        assertNotNull(n.requestedAtKst());
        assertNull(n.approvedAtKst());
    }

    @Test
    void 미지원_결제수단일_경우_CUSTOMEXCEPTION처리_된다() {
        Map<String, Object> source = resp(
                "휴대폰결제",   // fromKorean에서 예외
                1000, 0, "2025-08-24T12:00:00+09:00", "2025-08-24T12:01:00+09:00"
        );

        CustomException ex = assertThrows(CustomException.class,
                () -> normalizer.normalize(source, "pay_x"));
         assertEquals(ErrorCode.UNSUPPORTED_PAYMENT_METHOD, ex.getErrorCode());

        verifyNoInteractions(paymentsTypeRepository); // enum 변환 전에 이미 터져야 함
    }

    @Test
    void 결제수단은_유효하지만_repository에서_없으면_CUSTOMEXCEPTION이_터진다() {
        // given
        when(paymentsTypeRepository.findByTypeName(PaymentMethodType.CARD))
                .thenReturn(Optional.empty());

        Map<String, Object> source = resp(
                "카드",
                1000, 0, "2025-08-24T12:00:00+09:00", "2025-08-24T12:01:00+09:00"
        );

        // when / then
        assertThrows(CustomException.class,
                () -> normalizer.normalize(source, "pay_no_type"));

        verify(paymentsTypeRepository).findByTypeName(PaymentMethodType.CARD);
    }
}