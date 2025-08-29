package org.example.sansam.payment.domain;

import org.example.sansam.exception.pay.CustomException;
import org.example.sansam.order.domain.Order;
import org.example.sansam.status.domain.Status;
import org.example.sansam.status.domain.StatusEnum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;


import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class PaymentsTest {

    private static Order dummyOrder() {
        return mock(Order.class);
    }

    private static PaymentsType dummyPaymentsType() {
        PaymentsType type = new PaymentsType();
        return type;
    }

    static Stream<String> nullRequiredKeys() {
        return Stream.of("order", "paymentsType", "paymentKey", "totalPrice", "requestedAt", "approvedAt");
    }


    @Test
    void create로_payment객체를_생성할_수_있다() {
        // given
        Order order = dummyOrder();
        PaymentsType type = dummyPaymentsType();
        String paymentKey = "pay_123";
        Long totalPrice = 10_000L;
        Long finalPrice = 0L;
        LocalDateTime requestedAt = LocalDateTime.now();
        LocalDateTime approvedAt  = LocalDateTime.now();
        Status status = new Status(StatusEnum.PAYMENT_COMPLETED);

        // when
        Payments p = Payments.create(order, type, paymentKey, totalPrice, finalPrice, requestedAt, approvedAt,status);

        // then
        assertNotNull(p);
        assertEquals(order, p.getOrder());
        assertEquals(type, p.getPaymentsType());
        assertEquals(paymentKey, p.getPaymentKey());
        assertEquals(totalPrice, p.getTotalPrice());
        assertEquals(finalPrice, p.getFinalPrice());
        assertEquals(requestedAt, p.getRequestedAt());
        assertEquals(approvedAt, p.getApprovedAt());
        assertEquals(StatusEnum.PAYMENT_COMPLETED, p.getStatus().getStatusName());
    }

    @ParameterizedTest(name = "필수값 {0} == null이면 CustomException")
    @MethodSource("nullRequiredKeys")
    void create_throws_whenRequiredIsNull(String nullKey) {
        //given
        Order order         = "order".equals(nullKey) ? null : dummyOrder();
        PaymentsType type   = "paymentsType".equals(nullKey) ? null : dummyPaymentsType();
        String paymentKey   = "paymentKey".equals(nullKey) ? null : "pay_123";
        Long totalPrice     = "totalPrice".equals(nullKey) ? null : 1000L;
        Long finalPrice     = 0L;
        LocalDateTime now   = LocalDateTime.now();
        LocalDateTime req   = "requestedAt".equals(nullKey) ? null : now;
        LocalDateTime appr  = "approvedAt".equals(nullKey) ? null : now;
        Status status       = new Status(StatusEnum.PAYMENT_COMPLETED);

        //when & then
        assertThrows(CustomException.class, () ->
                Payments.create(order, type, paymentKey, totalPrice, finalPrice, req, appr, status)
        );
    }

}