package org.example.sansam.payment.compensation.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.example.sansam.payment.compensation.domain.OutBoxStatus;
import org.example.sansam.payment.compensation.domain.PaymentCancelOutBox;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PaymentCancelOutBoxRepositoryTest {

    @Autowired
    PaymentCancelOutBoxRepository repo;

    @PersistenceContext
    EntityManager em;

    private static void set(Object target, String field, Object value) {
        try {
            Field f = target.getClass().getDeclaredField(field);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private PaymentCancelOutBox persistBox(
            String idempotencyKey,
            OutBoxStatus status,
            LocalDateTime nextRunAt,
            String lockedBy
    ) {
        PaymentCancelOutBox o = PaymentCancelOutBox.create("pay_" + idempotencyKey, 1000L, "reason", idempotencyKey);
        if (status != null) set(o, "status", status);
        if (nextRunAt != null) set(o, "nextRunAt", nextRunAt);
        if (lockedBy != null) {
            set(o, "lockedBy", lockedBy);
            set(o, "lockedAt", LocalDateTime.now());
        }
        o = repo.save(o);
        em.flush();
        return o;
    }

    private static LocalDateTime trunc(LocalDateTime t) {
        return t.truncatedTo(ChronoUnit.SECONDS);
    }

    @AfterEach
    void tearDown() {
        repo.deleteAll();
        em.flush();
        em.clear();
    }


    @Test
    void findRunnable은_status가_Pending이거나_nextRunAt이_now이하만_조회() {

        //given
        LocalDateTime now = LocalDateTime.now();
        PaymentCancelOutBox a = persistBox("a", OutBoxStatus.PENDING, now.minusMinutes(5), null);
        PaymentCancelOutBox b = persistBox("b", OutBoxStatus.PENDING, now.minusMinutes(1), null);

        persistBox("c", OutBoxStatus.PENDING, now.plusMinutes(1), null);
        persistBox("d", OutBoxStatus.CLAIMED, now.minusMinutes(10), "worker1");
        persistBox("e", OutBoxStatus.FAILED,  now.minusMinutes(10), null);
        persistBox("f", OutBoxStatus.SUCCEEDED, now.minusMinutes(10), null);

        //when & then
        // page size 1 → only the oldest due (a)
        List<PaymentCancelOutBox> page1 = repo.findRunnable(now, PageRequest.of(0, 1));
        assertThat(page1).extracting(PaymentCancelOutBox::getId).containsExactly(a.getId());

        // page size 10 → [a, b]
        List<PaymentCancelOutBox> all = repo.findRunnable(now, PageRequest.of(0, 10));
        assertThat(all).extracting(PaymentCancelOutBox::getId).containsExactly(a.getId(), b.getId());


        assertThat(all.get(0).getNextRunAt()).isBeforeOrEqualTo(all.get(1).getNextRunAt());
    }

    @Test
    void 이미_잠금이거나_Pending이_아니면_성공() {
        //given
        LocalDateTime nowParam = trunc(LocalDateTime.now());
        PaymentCancelOutBox o = persistBox("claim-ok", OutBoxStatus.PENDING, nowParam.minusMinutes(1), null);

        //when & then
        int updated = repo.claim(o.getId(), "worker-42", nowParam);
        assertThat(updated).isEqualTo(1);

        em.clear();
        PaymentCancelOutBox reloaded = repo.findById(o.getId()).orElseThrow();

        assertThat(reloaded.getStatus()).isEqualTo(OutBoxStatus.CLAIMED);
        assertThat(reloaded.getLockedBy()).isEqualTo("worker-42");
        // +1초까지는 허용하는게 맞다는 판단
        assertThat(trunc(reloaded.getLockedAt())).isEqualTo(nowParam);
        assertThat(trunc(reloaded.getUpdatedAt())).isEqualTo(nowParam);
    }

    @Test
    void 이미_CLAIMED_상태가_된_것에_대해서는_실패한다() {
        //given
        LocalDateTime now = LocalDateTime.now();
        PaymentCancelOutBox o = persistBox("already-claimed", OutBoxStatus.CLAIMED, now.minusMinutes(1), "w1");

        //when & then
        int updated = repo.claim(o.getId(), "w2", trunc(now));
        assertThat(updated).isEqualTo(0);

        em.clear();
        PaymentCancelOutBox reloaded = repo.findById(o.getId()).orElseThrow();
        assertThat(reloaded.getStatus()).isEqualTo(OutBoxStatus.CLAIMED);
        assertThat(reloaded.getLockedBy()).isEqualTo("w1"); // 유지
    }

    @Test
    void Pending되어있는데_잠겨있으면_실패한다() {
        //given
        LocalDateTime now = LocalDateTime.now();
        PaymentCancelOutBox o = persistBox("pending-locked", OutBoxStatus.PENDING, now.minusMinutes(1), "w1");

        //when & then
        int updated = repo.claim(o.getId(), "w2", trunc(now));
        assertThat(updated).isEqualTo(0);

        em.clear();
        PaymentCancelOutBox reloaded = repo.findById(o.getId()).orElseThrow();
        assertThat(reloaded.getStatus()).isEqualTo(OutBoxStatus.PENDING);
        assertThat(reloaded.getLockedBy()).isEqualTo("w1"); // 그대로
    }

    @Test
    void Pending되어있지_않으면_실패한다() {
        //given
        LocalDateTime now = LocalDateTime.now();
        PaymentCancelOutBox s = persistBox("succ", OutBoxStatus.SUCCEEDED, now.minusMinutes(1), null);
        PaymentCancelOutBox f = persistBox("fail", OutBoxStatus.FAILED, now.minusMinutes(1), null);

        //when & then
        int u1 = repo.claim(s.getId(), "w", trunc(now));
        int u2 = repo.claim(f.getId(), "w", trunc(now));

        assertThat(u1).isEqualTo(0);
        assertThat(u2).isEqualTo(0);
    }

    @Test
    void ID가_찾아지지_않으면_실패한다() {
        //when & then
        int updated = repo.claim(9_999_999L, "w", trunc(LocalDateTime.now()));
        assertThat(updated).isEqualTo(0);
    }

    @Test
    void Claim은_두번_호출될_수_없다() {
        //given
        LocalDateTime now = trunc(LocalDateTime.now());
        PaymentCancelOutBox o = persistBox("twice", OutBoxStatus.PENDING, now.minusMinutes(1), null);

        //when
        int first = repo.claim(o.getId(), "w1", now);
        int second = repo.claim(o.getId(), "w2", now);

        //then
        assertThat(first).isEqualTo(1);
        assertThat(second).isEqualTo(0);

        em.clear();
        PaymentCancelOutBox reloaded = repo.findById(o.getId()).orElseThrow();
        assertThat(reloaded.getStatus()).isEqualTo(OutBoxStatus.CLAIMED);
        assertThat(reloaded.getLockedBy()).isEqualTo("w1");
        assertThat(trunc(reloaded.getLockedAt())).isEqualTo(now);
    }

    @Test
    void Runnable한_것_찾아서_실행시키고_이미_CLAIMED된_것에_대해서는_조회대상에서_제외한다() {
        //given
        LocalDateTime now = LocalDateTime.now();
        PaymentCancelOutBox a = persistBox("r1", OutBoxStatus.PENDING, now.minusMinutes(5), null);
        PaymentCancelOutBox b = persistBox("r2", OutBoxStatus.PENDING, now.minusMinutes(3), null);

        // when & then
        List<PaymentCancelOutBox> before = repo.findRunnable(now, PageRequest.of(0, 10));
        assertThat(before).extracting(PaymentCancelOutBox::getId).containsExactly(a.getId(), b.getId());

        int updated = repo.claim(a.getId(), "worker", trunc(now));
        assertThat(updated).isEqualTo(1);

        // claimed는 조회 대상에서 제외됨
        List<PaymentCancelOutBox> after = repo.findRunnable(now, PageRequest.of(0, 10));
        assertThat(after).extracting(PaymentCancelOutBox::getId).containsExactly(b.getId());
    }




}