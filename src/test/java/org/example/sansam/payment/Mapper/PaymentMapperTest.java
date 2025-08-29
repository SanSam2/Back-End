package org.example.sansam.payment.Mapper;

import org.example.sansam.payment.domain.PaymentMethodType;
import org.example.sansam.payment.domain.Payments;
import org.example.sansam.payment.domain.PaymentsType;
import org.example.sansam.payment.dto.TossPaymentResponse;
import org.example.sansam.status.domain.Status;
import org.example.sansam.status.domain.StatusEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@DataJpaTest(showSql = true)
class PaymentMapperTest {

    private PaymentMapper mapper;
    private Status paymentCompleted;
    private LocalDateTime now;
    private org.example.sansam.order.domain.Order dummyOrder;

    @BeforeEach
    void setUp() {
        // 생성된 구현체를 직접 사용 (스프링 컨텍스트 불필요)
        mapper = new PaymentMapperImpl();

        paymentCompleted = new Status(StatusEnum.PAYMENT_COMPLETED);
        now = LocalDateTime.now();
        dummyOrder = mock(org.example.sansam.order.domain.Order.class);
    }

    private PaymentsType typeOf(PaymentMethodType t) {
        PaymentsType pt = new PaymentsType();
        ReflectionTestUtils.setField(pt, "typeName", t);
        return pt;
    }

    private Payments paymentOf(PaymentMethodType t) {
        return Payments.create(
                dummyOrder,
                typeOf(t),
                "pay_123",
                12_000L,          // totalPrice
                3_000L,           // finalPrice
                now,              // requestedAt
                now,              // approvedAt
                paymentCompleted
        );
    }

    @Test
    void 전체필드가_정확히_매핑된다() {
        //given
        Payments p = paymentOf(PaymentMethodType.CARD);

        //when
        TossPaymentResponse dto = mapper.toTossPaymentResponse(p);

        //then
        assertNotNull(dto);
        assertEquals("카드", dto.getMethod());
        assertEquals(12_000L, dto.getTotalAmount());
        assertEquals(3_000L,  dto.getFinalAmount());
        assertEquals(p.getApprovedAt(), dto.getApprovedAt());
    }

    @ParameterizedTest
    @EnumSource(PaymentMethodType.class)
    void 모든enum값에_대해_잘_매핑된다(PaymentMethodType t) {
        //given&when
        TossPaymentResponse dto = mapper.toTossPaymentResponse(paymentOf(t));
        //then
        assertEquals(t.toKorean(), dto.getMethod());
    }

    @Test
    void methodType이나_typeName이_null이면_null오류가_터진다() {
        // case 1) paymentsType 자체가 null
        //given
        Payments p1 = new Payments();
        //when
        TossPaymentResponse dto1 = mapper.toTossPaymentResponse(p1);

        //then
        assertNotNull(dto1);
        assertNull(dto1.getMethod());

        // case 2) paymentsType는 있지만 typeName이 null
        //given
        Payments p2 = new Payments();
        PaymentsType pt = new PaymentsType();
        ReflectionTestUtils.setField(p2, "paymentsType", pt);

        //when
        TossPaymentResponse dto2 = mapper.toTossPaymentResponse(p2);

        //then
        assertNotNull(dto2);
        assertNull(dto2.getMethod());
    }

    @Test
    void source가_null이면_Null을_그대로_반환한다() {
        //given & when & then
        assertNull(mapper.toTossPaymentResponse(null));
    }

    @Test
    void mapMethod_payment_null이면_null() {
        assertNull(mapper.mapMethod(null));
    }

    @Test
    void toKorean이_예외면_enum_name을_반환한다() {
        Payments p = new Payments();
        PaymentsType pt = new PaymentsType();

        // enum 모킹(Inline Mock Maker 필요)
        PaymentMethodType broken = mock(PaymentMethodType.class);
        when(broken.toKorean()).thenThrow(new RuntimeException("boom"));
        when(broken.name()).thenReturn("CARD");

        // 내부 필드 주입
        ReflectionTestUtils.setField(pt, "typeName", broken);
        ReflectionTestUtils.setField(p, "paymentsType", pt);

        TossPaymentResponse dto = mapper.toTossPaymentResponse(p);

        assertNotNull(dto);
        assertEquals("CARD", dto.getMethod());
    }

}