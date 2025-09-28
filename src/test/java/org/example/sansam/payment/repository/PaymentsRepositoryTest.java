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
}