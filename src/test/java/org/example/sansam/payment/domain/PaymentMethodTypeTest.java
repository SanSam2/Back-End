package org.example.sansam.payment.domain;

import org.example.sansam.exception.pay.CustomException;
import org.example.sansam.exception.pay.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PaymentMethodTypeTest {

    @ParameterizedTest
    @CsvSource({
            "카드, CARD",
            "간편결제, EASY_PAY",
            "계좌이체, TRANSFER",
            "가상계좌, VIRTUAL_ACCOUNT"
    })
    void fromKorean으로_한국어결제method를_enum타입으로_변경한다(String inputKor, PaymentMethodType expected) {
        //given => Parameter when & then
        assertEquals(expected, PaymentMethodType.fromKorean(inputKor));
    }

    @ParameterizedTest
    @CsvSource({
            "CARD, 카드",
            "EASY_PAY, 간편결제",
            "TRANSFER, 계좌이체",
            "VIRTUAL_ACCOUNT, 가상계좌"
    })
    void toKorean으로_enum타입을_한국어결제method로_변경한다(PaymentMethodType input, String expectedKor) {
        assertEquals(expectedKor, input.toKorean());
    }

    @Test
    void enum에서_한글_한글에서_enum으로_원상복구_가능하다() {
        for (PaymentMethodType t : PaymentMethodType.values()) {
            //when
            String kor = t.toKorean();
            PaymentMethodType back = PaymentMethodType.fromKorean(kor);
            //then
            assertSame(t, back);
        }
    }

    @Test
    void toKorean결과_각문자열은_enum에_대해_서로겹치지_않는다() {
        //given
        Set<String> korSet = new HashSet<>();
        //when & then
        for (PaymentMethodType t : PaymentMethodType.values()) {
            assertTrue(korSet.add(t.toKorean()), "중복 한글표기: " + t.toKorean());
        }
    }

    @ParameterizedTest
    @CsvSource({
            "\"휴대폰결제\"",
            "\"카드 \"",
            "\" \"",
            "\"CARD\"",
            "\"카 드\"",
            "\"카-드\""
    })
    void fromKorean에서_지원하지_않는_결제수단이면_예외를_던진다(String badInput) {
        CustomException ex = assertThrows(CustomException.class,
                () -> PaymentMethodType.fromKorean(badInput));
         assertEquals(ErrorCode.UNSUPPORTED_PAYMENT_METHOD, ex.getErrorCode());
    }

    @Test
    void FromKorean에서_null값에_대해_CustomException을_발생시킨다() {
        assertThrows(CustomException.class, () -> PaymentMethodType.fromKorean(ErrorCode.UNSUPPORTED_PAYMENT_METHOD.getMessage()));
    }

    @Test
    void fromKorean_null이면_CustomException() {
        CustomException ex = assertThrows(CustomException.class,
                () -> PaymentMethodType.fromKorean(null));
        assertEquals(ErrorCode.UNSUPPORTED_PAYMENT_METHOD, ex.getErrorCode());
    }
}