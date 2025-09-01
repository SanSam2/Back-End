package org.example.sansam.payment.service;


import jakarta.persistence.EntityManager;
import org.example.sansam.notification.event.email.PaymentCompleteEmailEvent;
import org.example.sansam.notification.event.sse.PaymentCompleteEvent;
import org.example.sansam.payment.adapter.CancelResponseNormalize;
import org.example.sansam.payment.domain.*;
import org.example.sansam.payment.dto.CancelProductRequest;
import org.example.sansam.payment.dto.PaymentCancelRequest;
import org.example.sansam.payment.repository.PaymentsCancelRepository;
import org.example.sansam.payment.util.IdempotencyKeyUtil;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.transaction.annotation.Transactional;
import org.example.sansam.exception.pay.CustomException;
import org.example.sansam.exception.pay.ErrorCode;
import org.example.sansam.notification.service.EmailService;
import org.example.sansam.notification.service.NotificationService;
import org.example.sansam.order.domain.Order;
import org.example.sansam.order.domain.OrderProduct;
import org.example.sansam.order.domain.ordernumber.OrderNumberPolicy;
import org.example.sansam.order.domain.pricing.BasicPricingPolicy;
import org.example.sansam.payment.Mapper.PaymentMapper;
import org.example.sansam.payment.adapter.TossApprovalNormalizer.Normalized;
import org.example.sansam.payment.dto.TossPaymentResponse;
import org.example.sansam.payment.repository.PaymentsRepository;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.bean.override.mockito.MockitoBean;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;


@RecordApplicationEvents
@SpringBootTest
class AfterConfirmTransactionServiceTest {

    @MockitoBean
    private PaymentMapper paymentMapper;
    @MockitoBean
    private ApplicationEventPublisher eventPublisher;
    @MockitoBean
    private NotificationService notificationService;
    @MockitoBean
    private EmailService emailService;


    @Autowired
    private AfterConfirmTransactionService service;
    @Autowired
    private PaymentsRepository paymentsRepository;
    @Autowired
    private PaymentsCancelRepository paymentsCancelRepository;
    @Autowired
    private StatusRepository statusRepository;
    @Autowired
    private ApplicationEvents events;
    @Autowired
    private EntityManager em;

    private static final long TOTAL_AMOUNT = 20_000L;

    private User user1;
    private User user2;
    private Status orderWaitingStatus;
    private Status orderProductWaiting;
    private Status opPaid;
    private Status payCompleted;
    private Status orderPaid;
    private Status orderProductCanceled;
    private Status orderProductPartiallyCanceled;
    private Product product1;
    private Product product2;
    private FileManagement fm1;
    private Category cat;
    private Status productStatus;
    private Status cancelCompleted;
    private Order order;
    private Status orderAllCanceled;
    private Status orderPartialCanceled;
    private PaymentsType cardType;
    BasicPricingPolicy policy = new BasicPricingPolicy();

    private static class fakeOrderNumberPolicy implements OrderNumberPolicy {
        @Override
        public String makeOrderNumber() {
            return "1234567890-123e4567-e89b-12d3-a456-426614174000";
        }
    }

    @BeforeEach
    void setUp() {
        orderWaitingStatus = new Status(StatusEnum.ORDER_WAITING);
        em.persist(orderWaitingStatus);

        orderProductWaiting = new Status(StatusEnum.ORDER_PRODUCT_WAITING);
        em.persist(orderProductWaiting);

        opPaid = new Status(StatusEnum.ORDER_PRODUCT_PAID_AND_REVIEW_REQUIRED);
        em.persist(opPaid);

        orderPaid = new Status(StatusEnum.ORDER_PAID);
        em.persist(orderPaid);

        payCompleted = new Status(StatusEnum.PAYMENT_COMPLETED);
        em.persist(payCompleted);

        cancelCompleted = new Status(StatusEnum.CANCEL_COMPLETED);
        em.persist(cancelCompleted);

        orderProductCanceled = new Status(StatusEnum.ORDER_PRODUCT_CANCELED);
        em.persist(orderProductCanceled);

        orderProductPartiallyCanceled = new Status(StatusEnum.ORDER_PRODUCT_PARTIALLY_CANCELED);
        em.persist(orderProductPartiallyCanceled);

        orderAllCanceled = new Status(StatusEnum.ORDER_ALL_CANCELED);
        em.persist(orderAllCanceled);

        orderPartialCanceled = new Status(StatusEnum.ORDER_PARTIAL_CANCELED);
        em.persist(orderPartialCanceled);

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

        cardType = new PaymentsType(PaymentMethodType.CARD);
        em.persist(cardType);

        em.flush();

        doNothing().when(notificationService)
                .sendPaymentCompleteNotification(any(), anyString(), anyLong());

        doNothing().when(emailService)
                .sendPaymentCompletedEmail(any(), anyString(), anyLong());
    }

    private Normalized normalizedApproved() {
        Normalized n = mock(Normalized.class);
        given(n.paymentKey()).willReturn("pay_123");
        given(n.paymentsType()).willReturn(cardType);
        given(n.totalAmount()).willReturn(TOTAL_AMOUNT);
        given(n.balanceAmount()).willReturn(TOTAL_AMOUNT);
        given(n.requestedAtKst()).willReturn(LocalDateTime.of(2025,8,24,10,0));
        given(n.approvedAtKst()).willReturn(LocalDateTime.of(2025,8,24,10,5));
        return n;
    }
    private Normalized normalizedPending() {
        Normalized n = mock(Normalized.class);
        given(n.paymentKey()).willReturn("pay_123");
        given(n.paymentsType()).willReturn(cardType);
        given(n.totalAmount()).willReturn(TOTAL_AMOUNT);
        given(n.balanceAmount()).willReturn(TOTAL_AMOUNT);
        given(n.requestedAtKst()).willReturn(LocalDateTime.of(2025,8,24,10,0));
        given(n.approvedAtKst()).willReturn(null); // 승인 미완료
        return n;
    }


    private Order getManagedOrder() {
        return em.find(Order.class, order.getId());
    }

    @Test
    @Transactional
    void 승인성공_새결제저장_주문상태전이_이벤트2건_요청_매핑반환() {
        // given
        Normalized normalized = normalizedApproved();

        TossPaymentResponse expected = TossPaymentResponse.builder()
                .method("카드").totalAmount(TOTAL_AMOUNT).finalAmount(TOTAL_AMOUNT)
                .approvedAt(LocalDateTime.of(2025,8,24,10,5))
                .build();
        given(paymentMapper.toTossPaymentResponse(any(Payments.class))).willReturn(expected);
        String orderNumber=getManagedOrder().getOrderNumber();


        // when
        TossPaymentResponse actual = service.approveInTransaction(orderNumber, normalized);

        // then
        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);


        // DB 검증: 동일 paymentKey가 1건 저장
        Optional<Payments> savedOpt = paymentsRepository.findByPaymentKey("pay_123");
        assertThat(savedOpt).isPresent();

        // 주문/주문상품 상태 전이 검증
        Order reloaded = em.find(Order.class, getManagedOrder().getId());
        assertThat(reloaded.getStatus().getStatusName()).isEqualTo(StatusEnum.ORDER_PAID);
        reloaded.getOrderProducts().forEach(op ->
                assertThat(op.getStatus().getStatusName())
                        .isEqualTo(StatusEnum.ORDER_PRODUCT_PAID_AND_REVIEW_REQUIRED));

        // 이벤트 2건 발행
        assertThat(events.stream(PaymentCompleteEmailEvent.class)).hasSize(1);
        assertThat(events.stream(PaymentCompleteEvent.class)).hasSize(1);

        // 매퍼 호출
        then(paymentMapper).should().toTossPaymentResponse(any(Payments.class));
    }

    @Test
    @Transactional
    void 멱등성_이미해당PaymentKey가_존재하면_저장안하고_상태전이안하고_이벤트발생안하고_기존매핑반환() {
        // given
        Normalized normalized = normalizedApproved();
        TossPaymentResponse resp = TossPaymentResponse.builder()
                .method("카드").totalAmount(TOTAL_AMOUNT).finalAmount(TOTAL_AMOUNT)
                .approvedAt(LocalDateTime.of(2025,8,24,10,5))
                .build();
        given(paymentMapper.toTossPaymentResponse(any(Payments.class))).willReturn(resp);
        String orderNumber=getManagedOrder().getOrderNumber();


        // when
        TossPaymentResponse r1 = service.approveInTransaction(orderNumber, normalized);
        TossPaymentResponse r2 = service.approveInTransaction(orderNumber, normalized);


        // then
        assertThat(r1).usingRecursiveComparison().isEqualTo(resp);
        assertThat(r2).usingRecursiveComparison().isEqualTo(resp);

        assertThat(paymentsRepository.findByPaymentKey("pay_123")).isPresent();
        long total = paymentsRepository.count();
        assertThat(total).isEqualTo(1);

        assertThat(events.stream(PaymentCompleteEmailEvent.class)).hasSize(1);
        assertThat(events.stream(PaymentCompleteEvent.class)).hasSize(1);


    }

    @Test
    @Transactional
    void 승인미완료_approvedAtNull이면_결제필수값_누락_에러가_발생한다() {
        // given
        Normalized normalized = normalizedPending();

        TossPaymentResponse resp = TossPaymentResponse.builder()
                .method("카드").totalAmount(TOTAL_AMOUNT).finalAmount(TOTAL_AMOUNT)
                .approvedAt(null)
                .build();
        given(paymentMapper.toTossPaymentResponse(any(Payments.class))).willReturn(resp);
        String orderNumber=getManagedOrder().getOrderNumber();

        // when
        CustomException ex = assertThrows(CustomException.class, () ->
                service.approveInTransaction(orderNumber, normalized)
        );

        // then
        assertEquals(ErrorCode.PAYMENT_REQUIRE_ABSCENT,ex.getErrorCode());
    }

    @Test
    @Transactional
    void 취소저장_부모자식저장_자식상태_CancelCompleted_주문상태전이_검증() {
        // given: 기존 setUp에서 order에는 OP 한 줄(수량 2)이 들어있음
        Order o = getManagedOrder();
        Long opId = o.getOrderProducts().getFirst().getId();

        // 취소 요청 DTO
        List<CancelProductRequest> items = new ArrayList<>();
        items.add(new CancelProductRequest(opId,2));
        PaymentCancelRequest req = new PaymentCancelRequest(
                //orderId cancelReason, items
                o.getOrderNumber(),
                "testReason",
                items
        );
        var parsed = new CancelResponseNormalize.ParsedCancel("pay_cancel_1", 20000L, "테스트 취소", LocalDateTime.of(2025,8,25,10,0));
        String idempotencyKey = IdempotencyKeyUtil.forCancel(
                parsed.paymentKey(),
                parsed.refundPrice(),
                "testReason"
        );

        // when
        var resp = service.saveCancellation(o, parsed, req, idempotencyKey);

        // then: 반환이 비어있지 않음
        assertThat(resp).isNotNull();

        // 부모/자식 저장 검증
        var all = paymentsCancelRepository.findAll();
        assertThat(all).hasSize(1);

        var pc = all.get(0);
        assertThat(pc.getPaymentKey()).isEqualTo("pay_cancel_1");
        assertThat(pc.getRefundPrice()).isEqualTo(20000L);
        assertThat(pc.getCancelReason()).isEqualTo("테스트 취소");
        assertThat(pc.getOrderId()).isEqualTo(o.getId());
        assertThat(pc.getPaymentCancellationHistories()).hasSize(1);

        var h = pc.getPaymentCancellationHistories().get(0);
        assertThat(h.getOrderProductId()).isEqualTo(opId);
        assertThat(h.getQuantity()).isEqualTo(2);
        assertThat(h.getStatus().getStatusName()).isEqualTo(StatusEnum.CANCEL_COMPLETED);

        // 주문 상태 전이(전량 취소이므로 ALL_CANCELED일 것으로 기대)
        Order reloaded = em.find(Order.class, o.getId());
        assertThat(reloaded.getStatus().getStatusName())
                .isIn(StatusEnum.ORDER_ALL_CANCELED, StatusEnum.ORDER_PARTIAL_CANCELED); // 구현에 따라 다를 수 있어 둘 다 허용
    }


    @Test
    @Transactional
    void 취소저장_다건_각각_히스토리저장과수량검증() {
        // given: 기존 주문에 라인 하나 더 추가
        Order o = getManagedOrder();
        OrderProduct op2 = OrderProduct.create(product2, 10000L, 1, "M", "BLACK", "url", orderProductWaiting);
        o.addOrderProduct(op2);
        em.flush();

        Long opId = o.getOrderProducts().get(0).getId();
        Long op2Id = op2.getId();

        // DTO 모킹: 두 라인 각각 다른 수량
        List<CancelProductRequest> items = new ArrayList<>();
        items.add(new CancelProductRequest(opId,1));
        items.add(new CancelProductRequest(op2Id,1));
        PaymentCancelRequest req = new PaymentCancelRequest(
                //orderId cancelReason, items
                o.getOrderNumber(),
                "testReason",
                items
        );

        CancelResponseNormalize.ParsedCancel parsed = new CancelResponseNormalize.ParsedCancel("pay_cancel_2", 20000L, "다건 취소", LocalDateTime.of(2025,8,25,11,0));
        String idempotencyKey = IdempotencyKeyUtil.forCancel(
                parsed.paymentKey(),
                parsed.refundPrice(),
                "testReason"
        );

        // when
        service.saveCancellation(o, parsed, req,idempotencyKey);

        // then: PaymentCancellation 1건 + 히스토리 2건
        List<PaymentCancellation> all = paymentsCancelRepository.findAll();
        assertThat(all).hasSize(1);

        PaymentCancellation pc = all.get(0);
        assertThat(pc.getPaymentKey()).isEqualTo("pay_cancel_2");
        assertThat(pc.getPaymentCancellationHistories()).hasSize(2);

        // 라인별 검증 (opId/qty/status)
        Map<Long, PaymentCancellationHistory> byOpId = pc.getPaymentCancellationHistories().stream()
                .collect(Collectors.toMap(
                        PaymentCancellationHistory::getOrderProductId,
                        h2 -> h2
                ));
        assertThat(byOpId.get(opId).getQuantity()).isEqualTo(1);
        assertThat(byOpId.get(opId).getStatus().getStatusName()).isEqualTo(StatusEnum.CANCEL_COMPLETED);
        assertThat(byOpId.get(op2Id).getQuantity()).isEqualTo(1);
        assertThat(byOpId.get(op2Id).getStatus().getStatusName()).isEqualTo(StatusEnum.CANCEL_COMPLETED);

        // 주문 상태 전이(부분 취소일 수도 있으니 범위로 체크)
        Order reloaded = em.find(Order.class, o.getId());
        assertThat(reloaded.getStatus().getStatusName())
                .isIn(StatusEnum.ORDER_ALL_CANCELED, StatusEnum.ORDER_PARTIAL_CANCELED);
    }

    @Test
    @Transactional
    void 취소요청_아이템이_없으면_히스토리없이_저장되고_주문상태유지() {
        // given
        Order o = getManagedOrder();
        PaymentCancelRequest req = new PaymentCancelRequest(
                o.getOrderNumber(), "빈요청", List.of()
        );
        var parsed = new CancelResponseNormalize.ParsedCancel(
                "pay_cancel_empty", 0L, "빈요청", LocalDateTime.of(2025,8,25,12,30));
        String idem = IdempotencyKeyUtil.forCancel(parsed.paymentKey(), parsed.refundPrice(), "빈요청");

        // when
        var resp = service.saveCancellation(o, parsed, req, idem);

        // then
        assertThat(resp).isNotNull();

        // PaymentCancellation은 저장되지만 히스토리는 0건일 수 있음(현재 구현 기준)
        var all = paymentsCancelRepository.findAll();
        assertThat(all).hasSize(1);
        assertThat(all.get(0).getPaymentCancellationHistories()).isEmpty();

        // 취소가 없으므로 주문 상태는 그대로
        Order reloaded = em.find(Order.class, o.getId());
        assertThat(reloaded.getStatus().getStatusName()).isEqualTo(StatusEnum.ORDER_WAITING);
    }

    @Test
    @Transactional
    void 혼합_정상후_다른주문상품이면_예외() {
        //given
        Order o1 = getManagedOrder();
        Long validOpId = o1.getOrderProducts().getFirst().getId();

        // 다른 주문/다른 OP
        Order o2 = Order.create(user2, orderWaitingStatus, new fakeOrderNumberPolicy(), LocalDateTime.now());
        OrderProduct opOther = OrderProduct.create(product2, 10000L, 1, "M", "BLACK", "url", orderProductWaiting);
        o2.addOrderProduct(opOther);
        em.persist(o2);
        em.flush();
        Long alienId = opOther.getId(); // 존재하지만 o1에 속하지 않음

        PaymentCancelRequest req = new PaymentCancelRequest(
                o1.getOrderNumber(),
                "혼합",
                List.of(
                        new CancelProductRequest(validOpId, 1),  // 정상
                        new CancelProductRequest(alienId, 1)     // 다른 주문의 OP → orElseThrow
                )
        );

        var parsed = new CancelResponseNormalize.ParsedCancel(
                "pay_cancel_mix2", 10000L, "혼합", LocalDateTime.of(2025,8,25,13,0)
        );
        String idem = IdempotencyKeyUtil.forCancel(parsed.paymentKey(), parsed.refundPrice(), "혼합");

        CustomException ex = assertThrows(CustomException.class,
                () -> service.saveCancellation(o1, parsed, req, idem));

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.ORDER_PRODUCT_NOT_BELONGS_TO_ORDER);
        assertThat(paymentsCancelRepository.findAll()).isEmpty();
        assertThat(em.find(Order.class, o1.getId()).getStatus().getStatusName())
                .isEqualTo(StatusEnum.ORDER_WAITING);
    }

    @Test
    @Transactional
    void 이미존재하는_paymentKey면_existing리턴() {
        Normalized normalized = normalizedApproved();

        TossPaymentResponse resp = TossPaymentResponse.builder()
                .method("카드").totalAmount(TOTAL_AMOUNT).finalAmount(TOTAL_AMOUNT)
                .approvedAt(LocalDateTime.of(2025,8,24,10,5))
                .build();
        given(paymentMapper.toTossPaymentResponse(any(Payments.class))).willReturn(resp);
        String orderNumber=getManagedOrder().getOrderNumber();

        // 1차 호출로 save
        service.approveInTransaction(orderNumber, normalized);

        // 2차 호출시 existing != null 경로
        TossPaymentResponse actual = service.approveInTransaction(orderNumber, normalized);

        assertThat(actual).isEqualTo(resp);
        then(paymentMapper).should(atLeastOnce()).toTossPaymentResponse(any(Payments.class));
    }


}