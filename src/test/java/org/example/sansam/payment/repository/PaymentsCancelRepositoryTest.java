package org.example.sansam.payment.repository;

import jakarta.persistence.EntityManager;
import org.example.sansam.payment.domain.PaymentCancellation;
import org.example.sansam.payment.domain.PaymentCancellationHistory;
import org.example.sansam.status.domain.Status;
import org.example.sansam.status.domain.StatusEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith(SpringExtension.class) //DataJpaTest에 이미 내장되어있다.
@DataJpaTest(showSql = true)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PaymentsCancelRepositoryTest {

    @Autowired
    private PaymentsCancelRepository paymentsCancelRepository;

    @Autowired
    private EntityManager em;


    private Status cancelCompleted;

    @BeforeEach
    void init() {
        cancelCompleted = new Status(StatusEnum.CANCEL_COMPLETED);
        em.persist(cancelCompleted);
    }

    @Test
    void datasource_정상_check() {
        Object one = em.createNativeQuery("select 1").getSingleResult();
        assertThat(((Number) one).intValue()).isEqualTo(1);
    }


    @Test
    void PaymentCancel레포지토리로_저장할_수_있다() {
        // given
        PaymentCancellation pc = PaymentCancellation.create(
                "pk_1", 10000L, "테스트","testIdempotencyKey" ,42L, LocalDateTime.now()
        );
        pc.addCancellationHistory(
                PaymentCancellationHistory.create(1001L, 2, cancelCompleted)
        );

        // when
        PaymentCancellation saved = paymentsCancelRepository.save(pc);

        // then
        PaymentCancellation found = paymentsCancelRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getPaymentKey()).isEqualTo("pk_1");
        assertThat(found.getRefundPrice()).isEqualTo(10000L);
        assertThat(found.getIdempotencyKey()).isEqualTo("testIdempotencyKey");
        assertThat(found.getPaymentCancellationHistories()).hasSize(1);
        assertThat(found.getPaymentCancellationHistories().get(0).getOrderProductId()).isEqualTo(1001L);
    }

}