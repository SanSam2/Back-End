package org.example.sansam.payment.compensation.worker;

import jakarta.persistence.EntityManager;
import org.example.sansam.payment.compensation.domain.OutBoxStatus;
import org.example.sansam.payment.compensation.domain.PaymentCancelOutBox;
import org.example.sansam.payment.compensation.repository.PaymentCancelOutBoxRepository;
import org.example.sansam.payment.service.PaymentApiClient;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;


@SpringBootTest
@Transactional
class PaymentCancelWorkerTest {

    @Autowired
    PaymentCancelOutBoxRepository repo;
    @Autowired
    PaymentCancelWorker worker;
    @Autowired
    PaymentCancelProcessor processor;

    @MockitoBean
    PaymentApiClient paymentApiClient;

    @Autowired EntityManager em;


    private static LocalDateTime nowTruncated() {
        return LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    }

    private PaymentCancelOutBox reload(Long id) {
        em.flush();
        em.clear();
        return repo.findById(id).orElseThrow();
    }

    @Test
    void processOne_성공시_SUCCEEDED로_변경됨에_대한_확인() {
        Long id = repo.save(PaymentCancelOutBox.create("pay_ok", 1234L, "r", "idem-ok")).getId();
        repo.claim(id, "w1", nowTruncated());

        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        given(paymentApiClient.tossPaymentCancel(anyString(), any(Long.class), anyString(), anyString()))
                .willReturn(Map.of("status","CANCELED"));

        processor.processOne(id);

        PaymentCancelOutBox re = repo.findById(id).orElseThrow(); // 이제 DB에서 다시 읽음
        assertThat(re.getStatus()).isEqualTo(OutBoxStatus.SUCCEEDED);
    }

    @Test
    void processOne_실패시_PENDING전이_백오프_잠금해제() {
        // given
        Long id = repo.save(PaymentCancelOutBox.create("pay_ng", 5000L, "r", "idem-ng")).getId();
        repo.claim(id, "w1", nowTruncated());

        // 바깥 트랜잭션 커밋
        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        // 외부 API 실패 유도
        given(paymentApiClient.tossPaymentCancel(anyString(), any(Long.class), anyString(), anyString()))
                .willThrow(new RuntimeException("net down"));
        LocalDateTime before = nowTruncated();

        // when
        processor.processOne(id);

        // then
        PaymentCancelOutBox re = repo.findById(id).orElseThrow();
        assertThat(re.getStatus()).isEqualTo(OutBoxStatus.PENDING);
        assertThat(re.getAttempt()).isEqualTo(1);
        assertThat(re.getLockedBy()).isNull();
        assertThat(re.getLockedAt()).isNull();
        assertThat(re.getLastError()).contains("net down");

        // 백오프(여유 범위)
        assertThat(re.getNextRunAt()).isBetween(
                before.plusMinutes(2).minusSeconds(5),
                before.plusMinutes(2).plusSeconds(30)
        );

        then(paymentApiClient).should(times(1))
                .tossPaymentCancel(anyString(), any(Long.class), anyString(), anyString());
    }

    @Test
    void processOne_연속실패시_maxAttempt_도달하면_FAILED() {
        // given
        Long id = repo.save(PaymentCancelOutBox.create("pay_x", 1000L, "r", "idem-x")).getId();

        // API 항상 실패
        given(paymentApiClient.tossPaymentCancel(anyString(), any(Long.class), anyString(), anyString()))
                .willThrow(new RuntimeException("boom"));

        for (int i = 1; i <= 7; i++) {
            repo.claim(id, "w" + i, nowTruncated());

            // 커밋(CLAIM 확정)
            TestTransaction.flagForCommit();
            TestTransaction.end();
            TestTransaction.start();

            // when
            processor.processOne(id);

            // then (중간중간 확인)
            PaymentCancelOutBox cur = repo.findById(id).orElseThrow();
            if (i < 7) {
                assertThat(cur.getStatus()).isEqualTo(OutBoxStatus.PENDING);
                assertThat(cur.getAttempt()).isEqualTo(i);
            } else {
                assertThat(cur.getStatus()).isEqualTo(OutBoxStatus.FAILED);
                assertThat(cur.getAttempt()).isEqualTo(7);
            }
        }

        then(paymentApiClient).should(times(7))
                .tossPaymentCancel(anyString(), any(Long.class), anyString(), anyString());
    }

    @Test
    void processOne_API호출_파라미터검증() {
        // given
        var job = PaymentCancelOutBox.create("pay_ok2", 2222L, "db-failed", "idem-2222");
        Long id = repo.save(job).getId();
        repo.claim(id, "w1", nowTruncated());

        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        given(paymentApiClient.tossPaymentCancel(anyString(), any(Long.class), anyString(), anyString()))
                .willReturn(Map.of("status", "CANCELED"));

        // when
        processor.processOne(id);

        // then
        ArgumentCaptor<String> keyCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Long> amtCap = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<String> reasonCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> idemCap = ArgumentCaptor.forClass(String.class);

        then(paymentApiClient).should(times(1))
                .tossPaymentCancel(keyCap.capture(), amtCap.capture(), reasonCap.capture(), idemCap.capture());

        PaymentCancelOutBox re = repo.findById(id).orElseThrow();
        assertThat(keyCap.getValue()).isEqualTo(re.getPaymentKey());
        assertThat(amtCap.getValue()).isEqualTo(re.getAmount());
        assertThat(reasonCap.getValue()).isEqualTo(re.getReason());
        assertThat(idemCap.getValue()).isEqualTo(re.getIdempotencyKey());
        assertThat(re.getStatus()).isEqualTo(OutBoxStatus.SUCCEEDED);
    }

    @Test
    void processOne_존재하지않는_ID면_조용히_종료() {
        // given
        long notExists = 9_999_999L;

        // when & then: 예외 없어야 함
        processor.processOne(notExists);

        // 그리고 API 호출 없어야 함
        then(paymentApiClient).shouldHaveNoInteractions();
    }




}