package org.example.sansam.payment.service;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.example.sansam.exception.pay.CustomException;
import org.example.sansam.exception.pay.ErrorCode;
import org.example.sansam.order.domain.Order;
import org.example.sansam.order.domain.OrderProduct;
import org.example.sansam.order.domain.ordernumber.OrderNumberPolicy;
import org.example.sansam.order.domain.pricing.BasicPricingPolicy;
import org.example.sansam.payment.adapter.CancelResponseNormalize;
import org.example.sansam.payment.compensation.service.PaymentCancelOutBoxService;
import org.example.sansam.payment.dto.CancelProductRequest;
import org.example.sansam.payment.dto.CancelResponse;
import org.example.sansam.payment.dto.PaymentCancelRequest;
import org.example.sansam.payment.policy.CancellationPolicy;
import org.example.sansam.payment.util.IdempotencyKeyGenerator;
import org.example.sansam.payment.util.IdempotencyKeyUtil;
import org.example.sansam.product.domain.Category;
import org.example.sansam.product.domain.Product;
import org.example.sansam.s3.domain.FileManagement;
import org.example.sansam.status.domain.Status;
import org.example.sansam.status.domain.StatusEnum;
import org.example.sansam.status.repository.StatusRepository;
import org.example.sansam.user.domain.Role;
import org.example.sansam.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@Transactional
class PaymentCancelServiceTest {

    @Autowired
    PaymentCancelService service;
    @Autowired
    EntityManager em;
    @Autowired
    StatusRepository statusRepository;

    @MockitoBean
    PaymentApiClient paymentApiClient;
    @MockitoBean
    AfterConfirmTransactionService afterConfirmTransactionService;
    @MockitoBean
    CancellationPolicy cancellationPolicy;
    @MockitoBean
    CancelResponseNormalize normalizeResponse;

    String FIXED_IDEM = "idem-fixed-123";

    User user;
    Category category;
    Status orderWaiting, orderProductWaiting;
    Status available;
    Product p1, p2;
    Order order;
    OrderProduct op1, op2;

    BasicPricingPolicy pricing = new BasicPricingPolicy();

    static class FakeOrderNumberPolicy implements OrderNumberPolicy {
        @Override
        public String makeOrderNumber() {
            return "TEST-ORDER-1234567890";
        }
    }

    @BeforeEach
    void setUp() {
        // status seed
        orderWaiting = new Status(StatusEnum.ORDER_WAITING);
        orderProductWaiting = new Status(StatusEnum.ORDER_PRODUCT_WAITING);
        available = new Status(StatusEnum.AVAILABLE);

        em.persist(orderWaiting);
        em.persist(orderProductWaiting);
        em.persist(available);

        // user
        user = new User();
        user.setEmail("user@test.com");
        user.setName("user");
        user.setPassword("1234");
        user.setRole(Role.USER);
        user.setEmailAgree(true);
        user.setCreatedAt(LocalDateTime.now());
        em.persist(user);

        // category
        category = new Category();
        category.setBigName("TOPS");
        category.setMiddleName("TEE");
        category.setSmallName("BASIC");
        em.persist(category);

        // file
        FileManagement fm = new FileManagement();
        em.persist(fm);

        // products
        p1 = new Product();
        p1.setCategory(category);
        p1.setStatus(available);
        p1.setBrandName("BRAND");
        p1.setProductName("P1");
        p1.setPrice(10_000L);
        p1.setFileManagement(fm);
        em.persist(p1);

        p2 = new Product();
        p2.setCategory(category);
        p2.setStatus(available);
        p2.setBrandName("BRAND");
        p2.setProductName("P2");
        p2.setPrice(10_000L);
        p2.setFileManagement(fm);
        em.persist(p2);

        // order + lines
        order = Order.create(user, orderWaiting, new FakeOrderNumberPolicy(), LocalDateTime.now());
        op1 = OrderProduct.create(p1, 10_000L, 3, "M", "BLACK", "url", orderProductWaiting);
        op2 = OrderProduct.create(p2, 10_000L, 1, "M", "BLACK", "url", orderProductWaiting);
        order.addOrderProduct(op1);
        order.addOrderProduct(op2);
        order.calcTotal(pricing);

        em.persist(order);
        em.flush();

        // paymentKey 세팅(필드가 private이면 리플렉션 사용)
        setField(order, "paymentKey", "pay_abc_123");
        em.flush();

        // cancellationPolicy는 통과(실 구현 복잡도 차단)
        willDoNothing().given(cancellationPolicy).validate(any(Order.class), anyList());
    }
}