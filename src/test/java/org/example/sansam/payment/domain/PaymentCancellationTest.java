package org.example.sansam.payment.domain;

import org.example.sansam.status.domain.Status;
import org.example.sansam.status.domain.StatusEnum;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

class PaymentCancellationTest {

    @Test
    void create로_PaymentCancellation_객체를_만들_수_있다() {
        // given
        String paymentKey = "pk_123";
        long refundPrice = 10_000L;
        String reason = "단위 테스트";
        long orderId = 42L;
        LocalDateTime when = LocalDateTime.now();
        String idempotencyKey = "testIdempotencyKey";

        // when
        PaymentCancellation pc = PaymentCancellation.create(paymentKey, refundPrice, reason,idempotencyKey ,orderId, when);

        // then
        assertThat(pc.getId()).isNull();
        assertThat(pc.getPaymentKey()).isEqualTo(paymentKey);
        assertThat(pc.getRefundPrice()).isEqualTo(refundPrice);
        assertThat(pc.getCancelReason()).isEqualTo(reason);
        assertThat(pc.getIdempotencyKey()).isEqualTo(idempotencyKey);
        assertThat(pc.getOrderId()).isEqualTo(orderId);
        assertThat(pc.getCancelDateTime()).isEqualTo(when);
        assertThat(pc.getPaymentCancellationHistories()).isEmpty();
    }

    @Test
    void addCancellationHistory() {
        //given
        Status cancelCompleted = new Status(StatusEnum.CANCEL_COMPLETED);
        PaymentCancellation pc = PaymentCancellation.create(
                "pk", 100L, "r","testIdemPotencyKey" ,1L, LocalDateTime.now()
        );
        PaymentCancellationHistory h = PaymentCancellationHistory.create(
                99L, 2, cancelCompleted
        ); // Status가 null 허용이면

        //when
        pc.addCancellationHistory(h);

        //then
        assertThat(pc.getPaymentCancellationHistories()).hasSize(1)
                .first()
                .extracting(PaymentCancellationHistory::getOrderProductId)
                .isEqualTo(99L);

    }
}