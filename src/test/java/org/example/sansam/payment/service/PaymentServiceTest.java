package org.example.sansam.payment.service;

import jakarta.persistence.EntityManager;

import jakarta.transaction.Transactional;
import org.example.sansam.exception.pay.CustomException;
import org.example.sansam.exception.pay.ErrorCode;
import org.example.sansam.order.domain.Order;
import org.example.sansam.order.domain.OrderProduct;
import org.example.sansam.order.domain.ordernumber.OrderNumberPolicy;
import org.example.sansam.order.domain.pricing.BasicPricingPolicy;
import org.example.sansam.payment.adapter.TossApprovalNormalizer;
import org.example.sansam.payment.compensation.service.PaymentCancelOutBoxService;
import org.example.sansam.payment.dto.TossPaymentRequest;
import org.example.sansam.payment.dto.TossPaymentResponse;
import org.example.sansam.payment.util.IdempotencyKeyGenerator;
import org.example.sansam.payment.util.IdempotencyKeyUtil;
import org.example.sansam.product.domain.Category;
import org.example.sansam.product.domain.Product;
import org.example.sansam.s3.domain.FileManagement;
import org.example.sansam.status.domain.Status;
import org.example.sansam.status.domain.StatusEnum;
import org.example.sansam.user.domain.Role;
import org.example.sansam.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;
import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@Transactional
class PaymentServiceTest {

    @MockitoBean
    private PaymentApiClient paymentApiClient;
    @MockitoBean
    private TossApprovalNormalizer normalizer;
    @MockitoBean
    private AfterConfirmTransactionService afterConfirmTransactionService;
    @MockitoBean
    PaymentCancelOutBoxService outboxService;
    @MockitoBean
    IdempotencyKeyGenerator idemGen;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private EntityManager em;


    private static final String PAYMENT_KEY = "pay_123";
    private static final long TOTAL_AMOUNT = 20000L;
    private static final long FINAL_AMOUNT = 20000L;
    String FIXED_IDEM = "idem-fixed-123";


    private User user1;
    private User user2;
    private Status orderWaitingStatus;
    private Status orderProductWaiting;
    private Status orderPaid;
    private Product product1;
    private Product product2;
    private FileManagement fm1;
    private Category cat;
    private Status productStatus;
    private Order order;
    BasicPricingPolicy policy = new BasicPricingPolicy();

    private static class fakeOrderNumberPolicy implements OrderNumberPolicy {
        @Override
        public String makeOrderNumber() {
            return "1234567890-123e4567-e89b-12d3-a456-426614174000";
        }
    }

    private TossPaymentRequest buildRequest(String orderId, long amount, String paymentKey) {
        TossPaymentRequest request = new TossPaymentRequest(paymentKey, orderId, amount);
        return request;
    }

    private static Map<String, Object> resp(Object method, Object total, Object balance,
                                            Object requestedAt, Object approvedAt) {
        Map<String, Object> m = new HashMap<>();
        m.put("method", method);
        m.put("totalAmount", total);
        m.put("balanceAmount", balance);
        m.put("requestedAt", requestedAt);
        m.put("approvedAt", approvedAt);
        return m;
    }

    @BeforeEach
    void setUp() {
        orderWaitingStatus = new Status(StatusEnum.ORDER_WAITING);
        em.persist(orderWaitingStatus);

        orderProductWaiting = new Status(StatusEnum.ORDER_PRODUCT_WAITING);
        em.persist(orderProductWaiting);

        orderPaid = new Status(StatusEnum.ORDER_PAID);
        em.persist(orderPaid);

        user1 = new User();
        user1.setEmail("xeulbn@test.com");
        user1.setName("xeulbn");
        user1.setPassword("1234");
        user1.setRole(Role.USER);
        user1.setEmailAgree(true);
        user1.setCreatedAt(LocalDateTime.now());
        em.persist(user1);

        user2 = new User();
        user2.setEmail("sansam@test.com");
        user2.setName("sansam");
        user2.setPassword("1234");
        user2.setRole(Role.USER);
        user2.setEmailAgree(true);
        user2.setCreatedAt(LocalDateTime.now());
        em.persist(user2);

        cat = new Category();
        cat.setBigName("TOPS");
        cat.setMiddleName("TEE");
        cat.setSmallName("BASIC");
        em.persist(cat);

        productStatus = new Status(StatusEnum.AVAILABLE);
        em.persist(productStatus);

        fm1 = new FileManagement();
        em.persist(fm1);

        product1 = new Product();
        product1.setCategory(cat);
        product1.setStatus(productStatus);
        product1.setBrandName("NIKE");
        product1.setProductName("Air Tee 1");
        product1.setPrice(10000L);
        product1.setFileManagement(fm1);
        em.persist(product1);

        product2 = new Product();
        product2.setCategory(cat);
        product2.setStatus(productStatus);
        product2.setBrandName("NIKE");
        product2.setProductName("Air Tee 2");
        product2.setPrice(10000L);
        product2.setFileManagement(fm1);
        em.persist(product2);

        order = Order.create(user1, orderWaitingStatus, new fakeOrderNumberPolicy(), LocalDateTime.now());
        OrderProduct op1_o1 = OrderProduct.create(product1, 10000L, 2, "M", "BLACK", "url", orderProductWaiting);
        order.addOrderProduct(op1_o1);

        order.calcTotal(policy);
        em.persist(order);
        em.flush();

    }

    @Test
    void 주문이_존재하고_가격이_일치하여_Payment를_Confirm한다() throws Exception {
        // given
        TossPaymentRequest request = buildRequest("1234567890-123e4567-e89b-12d3-a456-426614174000", TOTAL_AMOUNT, PAYMENT_KEY);
        Map<String, Object> source = resp(
                "카드",
                TOTAL_AMOUNT,
                FINAL_AMOUNT,
                "2025-08-24T10:00:00+09:00",
                "2025-08-24T10:05:00+09:00"
        );
        when(paymentApiClient.confirmPayment(request)).thenReturn(source);
        TossApprovalNormalizer.Normalized normalized = mock(TossApprovalNormalizer.Normalized.class);
        when(normalized.paymentKey()).thenReturn(PAYMENT_KEY);
        when(normalizer.normalize(source, PAYMENT_KEY)).thenReturn(normalized);

        LocalDateTime approvedAt = OffsetDateTime.parse("2025-08-24T10:05:00+09:00").toLocalDateTime();
        TossPaymentResponse expected = TossPaymentResponse.builder()
                .method("카드")
                .totalAmount(TOTAL_AMOUNT)
                .finalAmount(FINAL_AMOUNT)
                .approvedAt(approvedAt)
                .build();
        when(afterConfirmTransactionService.approveInTransaction(any(Order.class), eq(normalized)))
                .thenReturn(expected);

        // when
        TossPaymentResponse actual = paymentService.confirmPayment(request);

        // then
        assertThat(actual.getMethod()).isEqualTo("카드");
        assertThat(actual.getTotalAmount()).isEqualTo(TOTAL_AMOUNT);
        assertThat(actual.getFinalAmount()).isEqualTo(FINAL_AMOUNT);
        assertThat(actual.getApprovedAt()).isEqualTo(approvedAt);

        verify(paymentApiClient, never()).tossPaymentCancel(anyString(), anyLong(), anyString(),anyString());
    }


    @Test
    void 주문이_존재하지_않는경우_CustomException을_발생시킨다() {
        // given
        // 존재하지 않는 주문번호로 요청
        TossPaymentRequest request = buildRequest("NOT_EXISTS_ORDER", TOTAL_AMOUNT, PAYMENT_KEY);

        // when
        CustomException ex = assertThrows(CustomException.class,
                () -> paymentService.confirmPayment(request));

        // then
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.ORDER_NOT_FOUND);

        //외부 호출 검증
        verify(paymentApiClient, never()).confirmPayment(any());
        verify(normalizer, never()).normalize(any(), anyString());
        verify(afterConfirmTransactionService, never()).approveInTransaction(any(), any());
        verify(paymentApiClient, never()).tossPaymentCancel(anyString(), anyLong(), anyString(),anyString());

    }

    @Test
    void 요청금액과_주문총액이_같지_않으면_ORDERANDPAYNOTEQUAL_오류가_발생한다() {
        // given
        String orderId = order.getOrderNumber();
        long wrongAmount = TOTAL_AMOUNT - 1;
        TossPaymentRequest request = buildRequest(orderId, wrongAmount, PAYMENT_KEY);

        // when
        CustomException ex = assertThrows(CustomException.class,
                () -> paymentService.confirmPayment(request));

        // then
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.ORDER_AND_PAY_NOT_EQUAL);
        // 금액 불일치 단계에서 바로 종료되어야 한다
        verify(paymentApiClient, never()).confirmPayment(any());
        verify(normalizer, never()).normalize(any(), anyString());
        verify(afterConfirmTransactionService, never()).approveInTransaction(any(), any());
        verify(paymentApiClient, never()).tossPaymentCancel(anyString(), anyLong(), anyString(),anyString());
    }

    @Test
    void DB트랜잭션의_처리가_실패하면_bestEffortCancel_호출후_Payment는_Failed된다() throws Exception {
        // given
        String orderId = order.getOrderNumber();
        TossPaymentRequest request = buildRequest(orderId, TOTAL_AMOUNT, PAYMENT_KEY);

        Map<String, Object> source = resp(
                "카드",
                TOTAL_AMOUNT,
                FINAL_AMOUNT,
                "2025-08-24T10:00:00+09:00",
                "2025-08-24T10:05:00+09:00"
        );
        when(paymentApiClient.confirmPayment(request)).thenReturn(source);

        TossApprovalNormalizer.Normalized normalized = mock(TossApprovalNormalizer.Normalized.class);
        when(normalized.paymentKey()).thenReturn(PAYMENT_KEY);
        when(normalized.totalAmount()).thenReturn(TOTAL_AMOUNT);

        when(normalizer.normalize(source, PAYMENT_KEY)).thenReturn(normalized);

        // 트랜잭션 서비스에서 런타임 예외 발생 → safeCancel 호출 후 PAYMENT_FAILED 던져야 함
        when(afterConfirmTransactionService.approveInTransaction(any(Order.class), eq(normalized)))
                .thenThrow(new RuntimeException("DB failure"));
        when(idemGen.forCancel(PAYMENT_KEY, TOTAL_AMOUNT, "db-failed"))
                .thenReturn(FIXED_IDEM);

        // when
        CustomException ex = assertThrows(CustomException.class,
                () -> paymentService.confirmPayment(request));

        // then
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.PAYMENT_FAILED);

        // 정상 호출
        verify(paymentApiClient).confirmPayment(request);
        verify(normalizer).normalize(source, PAYMENT_KEY);

        //idemGen호출검증
        verify(idemGen).forCancel(PAYMENT_KEY, TOTAL_AMOUNT, "db-failed");

        // ★ cancel 호출 시 FIXED_IDEM 이어야 함
        verify(paymentApiClient).tossPaymentCancel(PAYMENT_KEY, TOTAL_AMOUNT, "db-failed", FIXED_IDEM);

        // cancel 성공했으므로 outbox는 미호출
        verifyNoInteractions(outboxService);

    }

    @Test
    void 외부결제API에서_예외가_발생하면_상위로_전파된다() throws Exception {
        // given
        String orderId = order.getOrderNumber();
        TossPaymentRequest request = buildRequest(orderId, TOTAL_AMOUNT, PAYMENT_KEY);

        // 외부 결제 confirm 호출 시 예외 → 메서드 시그니처대로 상위 전파되어야 함
        when(paymentApiClient.confirmPayment(request))
                .thenThrow(new CustomException(ErrorCode.API_FAILED));

        // when & then
        CustomException ex = assertThrows(CustomException.class, () -> paymentService.confirmPayment(request));
        assertEquals(ex.getErrorCode(), ErrorCode.API_FAILED);

        // 이후 흐름은 호출되면 안 됨
        verify(normalizer, never()).normalize(any(), anyString());
        verify(afterConfirmTransactionService, never()).approveInTransaction(any(), any());
        verify(paymentApiClient, never()).tossPaymentCancel(anyString(), anyLong(), anyString(),anyString());

    }

    @Test
    void safeCancel내부에서_cancel실패해도_PAYMENT_FAILED는_그대로_보존된다() throws Exception {
        // given
        String orderId = order.getOrderNumber();
        TossPaymentRequest request = buildRequest(orderId, TOTAL_AMOUNT, PAYMENT_KEY);

        Map<String, Object> source = resp(
                "카드",
                TOTAL_AMOUNT,
                FINAL_AMOUNT,
                "2025-08-24T10:00:00+09:00",
                "2025-08-24T10:05:00+09:00"
        );
        when(paymentApiClient.confirmPayment(request)).thenReturn(source);

        TossApprovalNormalizer.Normalized normalized = mock(TossApprovalNormalizer.Normalized.class);
        when(normalized.paymentKey()).thenReturn(PAYMENT_KEY);
        when(normalizer.normalize(source, PAYMENT_KEY)).thenReturn(normalized);
        when(normalized.totalAmount()).thenReturn(TOTAL_AMOUNT);


        // 트랜잭션 실패 유도
        when(afterConfirmTransactionService.approveInTransaction(any(Order.class), eq(normalized)))
                .thenThrow(new RuntimeException("DB failure"));

        when(idemGen.forCancel(PAYMENT_KEY, TOTAL_AMOUNT, "db-failed"))
                .thenReturn(FIXED_IDEM);

        // cancel 자체가 실패하도록
        doThrow(new RuntimeException("cancel failed"))
                .when(paymentApiClient)
                .tossPaymentCancel(eq(PAYMENT_KEY), eq(TOTAL_AMOUNT), eq("db-failed"), eq(FIXED_IDEM));

        // when
        CustomException ex = assertThrows(CustomException.class,
                () -> paymentService.confirmPayment(request));

        // then
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.PAYMENT_FAILED);

        // idemGen 호출 검증
        verify(idemGen).forCancel(PAYMENT_KEY, TOTAL_AMOUNT, "db-failed");

        // cancel 시도는 FIXED_IDEM으로 호출됨
        verify(paymentApiClient).tossPaymentCancel(PAYMENT_KEY, TOTAL_AMOUNT, "db-failed", FIXED_IDEM);

        // cancel 실패했으니 outbox로 보관되어야 함 (같은 idemKey)
        verify(outboxService).enqueue(PAYMENT_KEY, TOTAL_AMOUNT, "db-failed", FIXED_IDEM);
    }
}
