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
        @Override public String makeOrderNumber() {
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


    @Test
    void 정상적으로_토스를_호출하고_키를_전파하며_저장위임까지_완료한다() {
        // given: op1=2개, op2=1개 → 총 30,000원
        PaymentCancelRequest req = new PaymentCancelRequest(
                order.getOrderNumber(),
                "테스트 취소",
                List.of(
                        new CancelProductRequest(op1.getId(), 2),
                        new CancelProductRequest(op2.getId(), 1)
                )
        );

        // Toss 취소 API 더미 응답
        Map<String, Object> tossRes = Map.of(
                "status", "CANCELED",
                "paymentKey", "pay_abc_123",
                "cancels", List.of(Map.of(
                        "transactionKey", "tx_1",
                        "cancelReason", "테스트 취소",
                        "canceledAt", "2025-08-25T11:00:00+09:00",
                        "cancelAmount", 30_000
                ))
        );
        given(paymentApiClient.tossPaymentCancel(eq("pay_abc_123"), eq(30_000L), eq("테스트 취소"), anyString()))
                .willReturn(tossRes);

        // 파서 결과를 고정(서비스가 그대로 saveCancellation에 넘기는지 보자)
        CancelResponseNormalize.ParsedCancel parsedFixed =
                new CancelResponseNormalize.ParsedCancel(
                        "pay_abc_123",
                        30_000L,
                        "테스트 취소",
                        LocalDateTime.of(2025, 8, 25, 11, 0)
                );
        given(normalizeResponse.parseTossCancelResponse(anyMap()))
                .willReturn(parsedFixed);

        // 저장 위임 결과 더미
        given(afterConfirmTransactionService.saveCancellation(eq(order), eq(parsedFixed), eq(req), anyString()))
                .willReturn(new CancelResponse("취소가 완료되었습니다."));

        // when
        CancelResponse resp = service.wantToCancel(req);

        // then: 응답 메시지
        assertThat(resp).isNotNull();
        assertThat(resp.getMessage()).isEqualTo("취소가 완료되었습니다.");

        // 1) Toss API로 넘긴 idemKey 캡처
        ArgumentCaptor<String> idemAtApi = ArgumentCaptor.forClass(String.class);
        then(paymentApiClient).should(times(1))
                .tossPaymentCancel(eq("pay_abc_123"), eq(30_000L), eq("테스트 취소"), idemAtApi.capture());

        // 2) saveCancellation으로 넘긴 idemKey 캡처
        ArgumentCaptor<String> idemAtSave = ArgumentCaptor.forClass(String.class);
        then(afterConfirmTransactionService).should(times(1))
                .saveCancellation(eq(order), eq(parsedFixed), eq(req), idemAtSave.capture());

        // 3) 두 호출에 동일 키가 전달되었는지
        assertThat(idemAtApi.getValue()).isNotBlank();
        assertThat(idemAtApi.getValue()).isEqualTo(idemAtSave.getValue());

        // 4) (선택) 유틸이 결정론적이라면 계산값과 정확 일치 검증
        String expected = IdempotencyKeyUtil.forCancel("pay_abc_123", 30_000L, "테스트 취소");
        assertThat(idemAtApi.getValue()).isEqualTo(expected);
    }

    @Test
    void request에_상품이_비어있으면_에러를_터트린다() {
        PaymentCancelRequest req = new PaymentCancelRequest(order.getOrderNumber(), "r", List.of());
        assertThatThrownBy(() -> service.wantToCancel(req))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_REQUEST);

        then(paymentApiClient).shouldHaveNoInteractions();
        then(afterConfirmTransactionService).shouldHaveNoInteractions();
    }

    @Test
    void paymentKey가_누락되면_오류를_터트린다() {
        Order managed = em.find(Order.class, order.getId());
        setField(managed, "paymentKey", null);
        em.flush();

        PaymentCancelRequest req = new PaymentCancelRequest(
                order.getOrderNumber(), "r", List.of(new CancelProductRequest(op1.getId(), 1))
        );

        assertThatThrownBy(() -> service.wantToCancel(req))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PAYMENTS_NOT_FOUND);

        then(paymentApiClient).shouldHaveNoInteractions();
        then(afterConfirmTransactionService).shouldHaveNoInteractions();
    }

    @Test
    void 주문상품이_주문과_연결되어있지_않으면_에러를_터트린다() {
        PaymentCancelRequest req = new PaymentCancelRequest(
                order.getOrderNumber(), "r", List.of(new CancelProductRequest(9_999_999L, 1))
        );

        assertThatThrownBy(() -> service.wantToCancel(req))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ORDER_PRODUCT_NOT_BELONGS_TO_ORDER);

        then(paymentApiClient).shouldHaveNoInteractions();
        then(afterConfirmTransactionService).shouldHaveNoInteractions();
    }

    @Test
    void opID가_null이면_INVALID_REQUEST가_난다_그리고_정책검증은_이미_호출됨() {
        PaymentCancelRequest req = new PaymentCancelRequest(
                order.getOrderNumber(), "r", List.of(new CancelProductRequest(null, 1))
        );

        assertThatThrownBy(() -> service.wantToCancel(req))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_REQUEST);

        then(paymentApiClient).shouldHaveNoInteractions();
        then(afterConfirmTransactionService).shouldHaveNoInteractions();

        // 서비스 흐름상 validate()는 먼저 호출됨
        then(cancellationPolicy).should().validate(any(Order.class), anyList());
    }

    @Test
    void request자체가_null이면_바로_INVALID_REQUEST_그리고_어떤_외부호출도_없다() {
        assertThatThrownBy(() -> service.wantToCancel(null))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_REQUEST);

        then(paymentApiClient).shouldHaveNoInteractions();
        then(afterConfirmTransactionService).shouldHaveNoInteractions();
        then(cancellationPolicy).shouldHaveNoInteractions();
    }

    @Test
    void request_items가_null이면_INVALID_REQUEST_그리고_외부호출없음() {
        // given
        PaymentCancelRequest req = new PaymentCancelRequest(
                order.getOrderNumber(),
                "사유",
                null
        );

        // when & then
        assertThatThrownBy(() -> service.wantToCancel(req))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_REQUEST);

        // 바로 종료되므로 어떤 외부작업도 없어야 함
        then(paymentApiClient).shouldHaveNoInteractions();
        then(afterConfirmTransactionService).shouldHaveNoInteractions();
        then(cancellationPolicy).shouldHaveNoInteractions();
    }
}