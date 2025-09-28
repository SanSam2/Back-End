package org.example.sansam.payment.compensation.service;

import org.example.sansam.payment.compensation.domain.OutBoxStatus;
import org.example.sansam.payment.compensation.domain.PaymentCancelOutBox;
import org.example.sansam.payment.compensation.repository.PaymentCancelOutBoxRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
class PaymentCancelOutBoxServiceTest {

    @Autowired
    PaymentCancelOutBoxService service;

    @Autowired
    PaymentCancelOutBoxRepository repo;

    @Autowired
    PlatformTransactionManager txManager;

    @AfterEach
    void tearDown() {
        repo.deleteAll();
    }

    @Test
    void enqueue_정상저장_및_기본필드값_검증() {
        // when
        LocalDateTime before = LocalDateTime.now();
        service.enqueue("pay_abc", 12_345L, "db-failed", "idem-001");
        LocalDateTime after = LocalDateTime.now();

        // then
        List<PaymentCancelOutBox> all = repo.findAll();
        assertThat(all).hasSize(1);

        PaymentCancelOutBox o = all.get(0);
        assertThat(o.getPaymentKey()).isEqualTo("pay_abc");
        assertThat(o.getAmount()).isEqualTo(12_345L);
        assertThat(o.getReason()).isEqualTo("db-failed");
        assertThat(o.getIdempotencyKey()).isEqualTo("idem-001");

        // 기본 상태값
        assertThat(o.getStatus()).isEqualTo(OutBoxStatus.PENDING);
        assertThat(o.getAttempt()).isZero();
        assertThat(o.getLockedBy()).isNull();
        assertThat(o.getLockedAt()).isNull();
        assertThat(o.getLastError()).isNull();

        // 시간 필드 범위(초 단위 허용)
        assertThat(o.getNextRunAt()).isBetween(before.minusSeconds(1), after.plusSeconds(1));
        assertThat(o.getCreatedAt()).isBetween(before.minusSeconds(1), after.plusSeconds(1));
        assertThat(o.getUpdatedAt()).isBetween(before.minusSeconds(1), after.plusSeconds(1));
    }

    @Test
    void enqueue는_REQUIRES_NEW라서_바깥_트랜잭션_롤백_영향을_받지_않는다() {
        //given
        TransactionTemplate tx = new TransactionTemplate(txManager);

        //when
        // 바깥 트랜잭션 내에서 enqueue 후 강제 예외 -> 바깥 트랜잭션은 롤백
        try {
            tx.execute(status -> {
                service.enqueue("pay_outer", 1000L, "r", "idem-newtx");
                throw new RuntimeException("outer rollback");
            });
            fail("예외가 발생해야 합니다");
        } catch (RuntimeException ignored) {
        }

        //then
        // REQUIRES_NEW 트랜잭션에서 커밋된 레코드는 살아 있어야 함
        List<PaymentCancelOutBox> all = repo.findAll();
        assertThat(all).extracting(PaymentCancelOutBox::getIdempotencyKey)
                .containsExactly("idem-newtx");
    }

    @Test
    void 같은_idempotencyKey로_두번_enqueue하면_유니크_제약으로_예외() {
        //given
        service.enqueue("pay_same", 100L, "r1", "idem-dup");

        //when & then
        assertThatThrownBy(() -> service.enqueue("pay_same2", 200L, "r2", "idem-dup"))
                .isInstanceOf(DataIntegrityViolationException.class);

        // 첫 번째 엔트리는 남아 있어야 함
        List<PaymentCancelOutBox> all = repo.findAll();
        assertThat(all).hasSize(1);
        assertThat(all.get(0).getAmount()).isEqualTo(100L);
        assertThat(all.get(0).getReason()).isEqualTo("r1");
    }
}