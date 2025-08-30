package org.example.sansam.payment.repository;

import jakarta.persistence.EntityManager;
import org.example.sansam.order.domain.Order;
import org.example.sansam.order.domain.ordernumber.OrderNumberPolicy;
import org.example.sansam.payment.domain.PaymentMethodType;
import org.example.sansam.payment.domain.Payments;
import org.example.sansam.payment.domain.PaymentsType;
import org.example.sansam.status.domain.Status;
import org.example.sansam.status.domain.StatusEnum;
import org.example.sansam.user.domain.Role;
import org.example.sansam.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@DataJpaTest(showSql = true)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PaymentsRepositoryTest {

    @Autowired
    private PaymentsRepository paymentsRepository;
    @Autowired
    private EntityManager em;

    private Status paymentCompleted;
    private Status orderWaitingStatus;
    private PaymentsType cardType;
    private User user1;
    private Order order;
    private static class fakeOrderNumberPolicy implements OrderNumberPolicy {

        @Override
        public String makeOrderNumber() {
            return "1234567890-123e4567-e89b-12d3-a456-426614174000";
        }
    }


    @BeforeEach
    void setUp() {
        // 1) 상태(영속)
        paymentCompleted = new Status(StatusEnum.PAYMENT_COMPLETED);
        orderWaitingStatus = new Status(StatusEnum.ORDER_WAITING);
        em.persist(paymentCompleted);
        em.persist(orderWaitingStatus);

        // 2) 결제수단(영속)
        cardType = new PaymentsType(PaymentMethodType.CARD);
        em.persist(cardType);

        user1 = new User();
        user1.setEmail("xeulbn@test.com");
        user1.setName("xeulbn");
        user1.setPassword("1234");
        user1.setRole(Role.USER);
        user1.setEmailAgree(true);
        user1.setCreatedAt(LocalDateTime.now());
        em.persist(user1);

        // 3) 주문(영속) — 반드시 네 Order의 필수 필드를 채워주세요
        order = Order.create(user1, orderWaitingStatus, new fakeOrderNumberPolicy(),  LocalDateTime.now());

        em.persist(order);
        em.flush();
    }



    @Test
    void findByPaymentKey() {
        //given
        String key = "pay_abc_123";
        LocalDateTime now = LocalDateTime.now();

        Payments saved = Payments.create(
                order,
                cardType,
                key,
                10_000L,          // totalPrice
                0L,               // finalPrice
                now,              // requestedAt
                now,              // approvedAt
                paymentCompleted  // status
        );
        em.persist(saved);

        // when
        Optional<Payments> found = paymentsRepository.findByPaymentKey(key);

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(saved.getId());
        assertThat(found.get().getPaymentKey()).isEqualTo(key);
        assertThat(found.get().getPaymentsType().getTypeName()).isEqualTo(PaymentMethodType.CARD);
        assertThat(found.get().getStatus().getStatusName()).isEqualTo(StatusEnum.PAYMENT_COMPLETED);
        assertThat(found.get().getOrder().getId()).isEqualTo(order.getId());
    }

    @Test
    void findByPaymentKey에서_paymentKey가_없다면_혹은_비어있다면() {
        //given & when
        Optional<Payments> found = paymentsRepository.findByPaymentKey("no_such_key");

        //then
        assertThat(found).isEmpty();
    }

}