package org.example.sansam.payment.compensation.domain;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;


class PaymentCancelOutBoxTest {

    private static void setPrivateInt(Object target, String field, int value) {
        try {
            Field f = target.getClass().getDeclaredField(field);
            f.setAccessible(true);
            f.setInt(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void setPrivateString(Object target, String field, String value) {
        try {
            Field f = target.getClass().getDeclaredField(field);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void assertBackoffMinutes(LocalDateTime callStart, LocalDateTime nextRunAt, long expectedMinutes) {
        long diff = ChronoUnit.MINUTES.between(callStart, nextRunAt);
        // 여유 1분 허용 (now 타이밍/초 단위 차이 보정)
        assertThat(diff)
                .as("nextRunAt should be about %d minutes later (actual: %d)", expectedMinutes, diff)
                .isBetween(expectedMinutes, expectedMinutes + 1);
    }

    @Test
    void create_기본값과_필드들이_올바르게_세팅된다() {
        // given
        LocalDateTime before = LocalDateTime.now();

        //when
        PaymentCancelOutBox o = PaymentCancelOutBox.create("pay_k1", 12_345L, "reason-x", "idem-1");
        LocalDateTime after  = LocalDateTime.now();

        // then
        assertThat(o.getPaymentKey()).isEqualTo("pay_k1");
        assertThat(o.getAmount()).isEqualTo(12_345L);
        assertThat(o.getReason()).isEqualTo("reason-x");
        assertThat(o.getIdempotencyKey()).isEqualTo("idem-1");

        assertThat(o.getStatus()).isEqualTo(OutBoxStatus.PENDING);
        assertThat(o.getAttempt()).isZero();
        assertThat(o.getMaxAttempt()).isEqualTo(7);

        assertThat(o.getLockedBy()).isNull();
        assertThat(o.getLockedAt()).isNull();
        assertThat(o.getLastError()).isNull();

        assertThat(o.getNextRunAt()).isBetween(before.minusSeconds(1), after.plusSeconds(1));
        assertThat(o.getCreatedAt()).isBetween(before.minusSeconds(1), after.plusSeconds(1));
        assertThat(o.getUpdatedAt()).isBetween(before.minusSeconds(1), after.plusSeconds(1));

        //생성 시간 검증
        assertThat(o.getUpdatedAt()).isAfterOrEqualTo(o.getCreatedAt());
    }

    @Test
    void markClaimed_잠금정보와_상태_업데이트() {
        // given
        PaymentCancelOutBox o = PaymentCancelOutBox.create("k", 1L, "r", "idem");
        LocalDateTime before = LocalDateTime.now();

        //when
        o.markClaimed("worker-1");
        LocalDateTime after = LocalDateTime.now();

        //then
        assertThat(o.getStatus()).isEqualTo(OutBoxStatus.CLAIMED);
        assertThat(o.getLockedBy()).isEqualTo("worker-1");
        assertThat(o.getLockedAt()).isBetween(before.minusSeconds(1), after.plusSeconds(1));
        assertThat(o.getUpdatedAt()).isBetween(before.minusSeconds(1), after.plusSeconds(1));
    }

    @Test
    void markSucceeded_상태_SUCCEEDED_및_updatedAt_갱신() {
        //given
        PaymentCancelOutBox o = PaymentCancelOutBox.create("k", 1L, "r", "idem");
        o.markClaimed("w");

        //when
        LocalDateTime prevUpdated = o.getUpdatedAt();
        o.markSucceeded();

        //then
        assertThat(o.getStatus()).isEqualTo(OutBoxStatus.SUCCEEDED);
        assertThat(o.getUpdatedAt()).isAfterOrEqualTo(prevUpdated);

        assertThat(o.getAttempt()).isZero();
        assertThat(o.getLockedBy()).isEqualTo("w");
        assertThat(o.getLockedAt()).isNotNull();
    }

    @Test
    void markFailedAndScheduleRetry_첫_재시도는_2분_후로_스케줄_그리고_잠금해제() {
        //given
        PaymentCancelOutBox o = PaymentCancelOutBox.create("k", 1L, "r", "idem");
        o.markClaimed("worker-x");
        LocalDateTime callStart = LocalDateTime.now();

        //when
        o.markFailedAndScheduleRetry("e1");

        //then
        assertThat(o.getStatus()).isEqualTo(OutBoxStatus.PENDING);
        assertThat(o.getAttempt()).isEqualTo(1);
        assertThat(o.getLastError()).isEqualTo("e1");
        assertBackoffMinutes(callStart, o.getNextRunAt(), 2);

        // 재시도 스케줄 분기에서는 lock 해제
        assertThat(o.getLockedBy()).isNull();
        assertThat(o.getLockedAt()).isNull();
    }

    @Test
    void markFailedAndScheduleRetry_지수백오프_2_4_8분() {
        //given
        PaymentCancelOutBox o = PaymentCancelOutBox.create("k", 1L, "r", "idem");

        // 1회차
        LocalDateTime call1 = LocalDateTime.now();
        o.markFailedAndScheduleRetry("e1");
        assertThat(o.getAttempt()).isEqualTo(1);
        assertBackoffMinutes(call1, o.getNextRunAt(), 2);

        // 2회차
        LocalDateTime call2 = LocalDateTime.now();
        o.markFailedAndScheduleRetry("e2");
        assertThat(o.getAttempt()).isEqualTo(2);
        assertBackoffMinutes(call2, o.getNextRunAt(), 4);

        // 3회차
        LocalDateTime call3 = LocalDateTime.now();
        o.markFailedAndScheduleRetry("e3");
        assertThat(o.getAttempt()).isEqualTo(3);
        assertBackoffMinutes(call3, o.getNextRunAt(), 8);
    }

    @Test
    void markFailedAndScheduleRetry_백오프는_10회에서_1024분으로_캡된다() {
        //given
        PaymentCancelOutBox o = PaymentCancelOutBox.create("k", 1L, "r", "idem");

        // when
        setPrivateInt(o, "maxAttempt", 50);
        setPrivateInt(o, "attempt", 9);
        LocalDateTime call = LocalDateTime.now();
        o.markFailedAndScheduleRetry("overflow");

        //then
        assertThat(o.getAttempt()).isEqualTo(10);
        assertThat(o.getStatus()).isEqualTo(OutBoxStatus.PENDING);
        assertBackoffMinutes(call, o.getNextRunAt(), 1024);
    }

    @Test
    void markFailedAndScheduleRetry_maxAttempt에_도달하면_FAILED로_전이되고_재스케줄_없음() {
        //given
        PaymentCancelOutBox o = PaymentCancelOutBox.create("k", 1L, "r", "idem");
        o.markClaimed("worker-fin");
        setPrivateInt(o, "attempt", 6);

        LocalDateTime prevNextRun = o.getNextRunAt();
        LocalDateTime prevLockedAt = o.getLockedAt();

        //when
        o.markFailedAndScheduleRetry("terminal-failure");

        //then
        assertThat(o.getAttempt()).isEqualTo(7);
        assertThat(o.getStatus()).isEqualTo(OutBoxStatus.FAILED);
        assertThat(o.getLastError()).isEqualTo("terminal-failure");

        // 재스케줄 분기가 아니므로 nextRunAt/lock은 그대로 보존
        assertThat(o.getNextRunAt()).isEqualTo(prevNextRun);
        assertThat(o.getLockedBy()).isEqualTo("worker-fin");
        assertThat(o.getLockedAt()).isEqualTo(prevLockedAt);

        // updatedAt은 증가
        assertThat(o.getUpdatedAt()).isAfterOrEqualTo(o.getCreatedAt());
    }

    @Test
    void 상태_변경_흐름() {
        //given
        PaymentCancelOutBox o = PaymentCancelOutBox.create("k", 1L, "r", "idem");

        // PENDING → CLAIMED
        o.markClaimed("w1");
        assertThat(o.getStatus()).isEqualTo(OutBoxStatus.CLAIMED);

        // CLAIMED → PENDING(+retry)
        LocalDateTime call = LocalDateTime.now();
        o.markFailedAndScheduleRetry("e");
        assertThat(o.getStatus()).isEqualTo(OutBoxStatus.PENDING);
        assertThat(o.getLockedBy()).isNull(); // retry 분기에서 lock 해제
        assertBackoffMinutes(call, o.getNextRunAt(), 2);

        // PENDING → SUCCEEDED
        o.markSucceeded();
        assertThat(o.getStatus()).isEqualTo(OutBoxStatus.SUCCEEDED);
    }


}