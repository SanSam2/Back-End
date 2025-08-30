package org.example.sansam.payment.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;


class DefaultIdempotencyKeyGeneratorTest {

    private final DefaultIdempotencyKeyGenerator gen = new DefaultIdempotencyKeyGenerator();

    @Test
    void forCancel_유틸값과_정확히_동일하다() {
        // given
        String paymentKey = "pay_123";
        long amount = 20_000L;
        String reason = "테스트 취소";

        // when
        String actual = gen.forCancel(paymentKey, amount, reason);

        // then
        String expected = IdempotencyKeyUtil.forCancel(paymentKey, amount, reason);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void 같은입력이면_항상_같은키가_생성된다() {
        // given
        String paymentKey = "pay_same";
        long amount = 999L;
        String reason = "same";

        // when
        String k1 = gen.forCancel(paymentKey, amount, reason);
        String k2 = gen.forCancel(paymentKey, amount, reason);

        // then
        assertThat(k1).isEqualTo(k2);
    }

    @Test
    void 입력이_하나라도_다르면_키가_달라진다() {
        // given
        String base = gen.forCancel("pay_A", 10L, "r");

        // paymentKey만 다름
        String diff1 = gen.forCancel("pay_B", 10L, "r");
        // amount만 다름
        String diff2 = gen.forCancel("pay_A", 11L, "r");
        // reason만 다름
        String diff3 = gen.forCancel("pay_A", 10L, "사유다름");

        // then
        assertThat(diff1).isNotEqualTo(base);
        assertThat(diff2).isNotEqualTo(base);
        assertThat(diff3).isNotEqualTo(base);
    }



    @Test
    void sha256Base64Url_고정벡터_검증() {
        // SHA-256("abc") 의 Base64URL(without padding) 기대값
        // 표준 base64:  "ungWv48Bz+pBQUDeXa4iI7ADYaOWF3qctBD/YfIAFa0"
        // URL-safe:    "ungWv48Bz-pBQUDeXa4iI7ADYaOWF3qctBD_YfIAFa0"
        String actual = IdempotencyKeyUtil.sha256Base64Url("abc");
        assertThat(actual).isEqualTo("ungWv48Bz-pBQUDeXa4iI7ADYaOWF3qctBD_YfIAFa0");
    }

    @Test
    void forCancel_사유정규화_NFKC_trim_공백압축_소문자화가_동일키를_만든다() {
        String pk = "pay_123";
        long amt = 20000L;

        // 원본
        String r1 = "Foo  BAR";
        // 공백 여러개 + 앞뒤 공백 + 탭/개행 섞기
        String r2 = " \tFoo   BAR \n";
        // 전각 문자(NFKC 필요) + 전각 스페이스(ideographic space)
        String r3 = "Ｆｏｏ　ＢＡＲ";
        // 대소문자 뒤섞임
        String r4 = "fOo bAr";

        String k1 = IdempotencyKeyUtil.forCancel(pk, amt, r1);
        String k2 = IdempotencyKeyUtil.forCancel(pk, amt, r2);
        String k3 = IdempotencyKeyUtil.forCancel(pk, amt, r3);
        String k4 = IdempotencyKeyUtil.forCancel(pk, amt, r4);

        // 정규화 결과가 같아야 같은 키
        assertThat(k2).isEqualTo(k1);
        assertThat(k3).isEqualTo(k1);
        assertThat(k4).isEqualTo(k1);
    }

    @Test
    void forCancel_정규화해도_다른사유면_키가_다르다() {
        String pk = "pay_123";
        long amt = 20000L;

        // "foo bar" vs "boo bar" (한 글자 차이)
        String a = IdempotencyKeyUtil.forCancel(pk, amt, "FOO   BAR");
        String b = IdempotencyKeyUtil.forCancel(pk, amt, "ＢＯＯ　ＢＡＲ"); // NFKC로 "boo bar"가 됨

        assertThat(b).isNotEqualTo(a);
    }

    @Test
    void sha256Base64Url_URL세이프_그리고_길이43() {
        String out = IdempotencyKeyUtil.sha256Base64Url("any");
        assertThat(out).matches("^[A-Za-z0-9_-]+$");
        assertThat(out).hasSize(43);
    }

}