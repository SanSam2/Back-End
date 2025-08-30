package org.example.sansam.payment.domain;

import org.example.sansam.status.domain.Status;
import org.example.sansam.status.domain.StatusEnum;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PaymentCancellationHistoryTest {

    @Test
    void create() {
        //given
        Long orderProductId = 1L;
        int quantity = 2;
        Status payComplete = new Status(StatusEnum.CANCEL_COMPLETED);

        //when
        PaymentCancellationHistory h = PaymentCancellationHistory.create(
                orderProductId, quantity, payComplete
        );

        //then
        assertNotNull(h);
        assertEquals(orderProductId, h.getOrderProductId());
        assertEquals(quantity, h.getQuantity());
        assertEquals(payComplete, h.getStatus());

    }

    @Test
    void changeStatusWhenCompleteCancel() {
        //given
        Long orderProductId = 1L;
        int quantity = 2;
        Status payComplete = new Status(StatusEnum.CANCEL_REFUNDED);
        Status payCompleted = new Status(StatusEnum.CANCEL_COMPLETED);
        PaymentCancellationHistory h = PaymentCancellationHistory.create(
                orderProductId, quantity, payComplete
        );

        //when
        h.changeStatusWhenCompleteCancel(payCompleted);

        //then
        assertEquals(payCompleted, h.getStatus());
    }

    @Test
    void changeStatusWhenCompleteRefund() {
        //given
        Long orderProductId = 1L;
        int quantity = 2;
        Status payComplete = new Status(StatusEnum.CANCEL_REFUNDED);
        Status payCompleted = new Status(StatusEnum.CANCEL_COMPLETED);
        PaymentCancellationHistory h = PaymentCancellationHistory.create(
                orderProductId, quantity, payComplete
        );

        //when
        h.changeStatusWhenCompleteRefund(payCompleted);

        //then
        assertEquals(payCompleted, h.getStatus());

    }
}