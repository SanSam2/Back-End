package org.example.sansam.payment.policy;

import org.example.sansam.exception.pay.CustomException;
import org.example.sansam.exception.pay.ErrorCode;
import org.example.sansam.order.domain.Order;
import org.example.sansam.order.domain.OrderProduct;
import org.example.sansam.order.domain.ordernumber.OrderNumberPolicy;
import org.example.sansam.payment.dto.CancelProductRequest;
import org.example.sansam.product.domain.Category;
import org.example.sansam.product.domain.Product;
import org.example.sansam.s3.domain.FileManagement;
import org.example.sansam.status.domain.Status;
import org.example.sansam.status.domain.StatusEnum;
import org.example.sansam.user.domain.Role;
import org.example.sansam.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;

class DefaultCancellationPolicyTest {

    DefaultCancellationPolicy policy;

    User user;
    Category category;
    FileManagement fm;
    Product p1, p2;

    Status orderWaiting;
    Status orderAllCanceled;
    Status orderProductWaiting;
    Status opCanceled;
    Status opPartialCanceled;
    Status opPaidAndReviewCompleted;
    Status available;

    Order order;
    OrderProduct op1; // qty 3
    OrderProduct op2; // qty 2

    static class FakeOrderNumberPolicy implements OrderNumberPolicy {
        @Override public String makeOrderNumber() {
            return "ORDER-TEST-0001";
        }
    }

    @BeforeEach
    void setUp() {
        policy = new DefaultCancellationPolicy();

        // 상태들
        orderWaiting = new Status(StatusEnum.ORDER_WAITING);
        orderAllCanceled = new Status(StatusEnum.ORDER_ALL_CANCELED);
        orderProductWaiting = new Status(StatusEnum.ORDER_PRODUCT_WAITING);
        opCanceled = new Status(StatusEnum.ORDER_PRODUCT_CANCELED);
        opPartialCanceled = new Status(StatusEnum.ORDER_PRODUCT_PARTIALLY_CANCELED);
        opPaidAndReviewCompleted = new Status(StatusEnum.ORDER_PRODUCT_PAID_AND_REVIEW_COMPLETED);
        available = new Status(StatusEnum.AVAILABLE);

        // 유저/카테고리/파일/상품
        user = new User();
        user.setEmail("user@test.com");
        user.setPassword("pw");
        user.setName("user");
        user.setRole(Role.USER);
        user.setEmailAgree(true);
        user.setCreatedAt(LocalDateTime.now());

        category = new Category();
        category.setBigName("TOPS"); category.setMiddleName("TEE"); category.setSmallName("BASIC");

        fm = new FileManagement();

        p1 = new Product();
        p1.setCategory(category);
        p1.setStatus(available);
        p1.setBrandName("BRAND");
        p1.setProductName("P1");
        p1.setPrice(10_000L);
        p1.setFileManagement(fm);

        p2 = new Product();
        p2.setCategory(category);
        p2.setStatus(available);
        p2.setBrandName("BRAND");
        p2.setProductName("P2");
        p2.setPrice(10_000L);
        p2.setFileManagement(fm);

        // 주문 + 라인(영속성X 순수 객체)
        order = Order.create(user, orderWaiting, new FakeOrderNumberPolicy(), LocalDateTime.now());

        op1 = OrderProduct.create(p1, 10_000L, 3, "M", "BLACK", "url", orderProductWaiting); // 수량 3
        op2 = OrderProduct.create(p2, 10_000L, 2, "M", "BLACK", "url", orderProductWaiting); // 수량 2

        order.addOrderProduct(op1);
        order.addOrderProduct(op2);

        // 테스트 편의상 라인 id 부여(정책은 id로 매칭하므로)
        setId(op1, 101L);
        setId(op2, 202L);
    }

    private static void setId(OrderProduct op, Long id) {
        setField(op, "id", id);
    }


    @Test
    void 주문후_24시간_이내_등_조건을만족시키는_부분에_대해서_결제취소가_가능하다() {
        //given
        // op1: 2개 취소, op2: 1개 취소 모두 가능
        List<CancelProductRequest> reqs = List.of(
                new CancelProductRequest(101L, 2),
                new CancelProductRequest(202L, 1)
        );

        //when & then
        assertThatCode(() -> policy.validate(order, reqs))
                .doesNotThrowAnyException();
    }

    @Test
    void order가_null인경우_orderNotFound_에러를_발생시킨다() {
        //given & when & then
        assertThatThrownBy(() -> policy.validate(null, List.of(new CancelProductRequest(101L, 1))))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.ORDER_NOT_FOUND);
    }

    @Test
    void request가_비어있으면_Empty에러를_발생시킨다() {
        //given & when & then
        assertThatThrownBy(() -> policy.validate(order, List.of()))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.CANCEL_NOT_FOUND);
    }

    @Test
    void order의_상태가_orderAllCanceled일경우_주문취소불가로_에러를_발생시킨다() {
        //given
        // 주문 상태를 ALL_CANCELED로 설정
        setField(order, "status", orderAllCanceled);

        //when & then
        assertThatThrownBy(() -> policy.validate(order, List.of(new CancelProductRequest(101L, 1))))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.ORDER_NOT_CANCELABLE);
    }

    @Test
    void 하루가_지난_주문건에_대해서_주문취소불가로_에러를_발생시킨다() {
        //given
        setField(order, "createdAt", LocalDateTime.now().minusHours(25));

        //when & then
        assertThatThrownBy(() -> policy.validate(order, List.of(new CancelProductRequest(101L, 1))))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.ORDER_NOT_CANCELABLE);
    }

    @Test
    void 주문후_정확히_24시간이면_시간제한에_걸리지_않고_통과한다() {
        // given: createdAt = now - 24h  (Duration.toHours()는 내림이라 24 → >24 조건 불충족)
        setField(order, "createdAt", LocalDateTime.now().minusHours(24));

        List<CancelProductRequest> reqs = List.of(
                new CancelProductRequest(101L, 1),
                new CancelProductRequest(202L, 1)
        );

        // when & then
        assertThatCode(() -> policy.validate(order, reqs))
                .doesNotThrowAnyException();
    }

    @Test
    void createdAt이_null이면_시간검증을_스킵하고_유효하면_통과한다() {
        // given: 시간제한 스킵 경로(baseTime == null)
        setField(order, "createdAt", null);

        List<CancelProductRequest> reqs = List.of(
                new CancelProductRequest(101L, 1),
                new CancelProductRequest(202L, 1)
        );

        // when & then
        assertThatCode(() -> policy.validate(order, reqs))
                .doesNotThrowAnyException();
    }

    @Test
    void item안에서_orderProductId가_null값이면_잘못된_요청_에러를_반환한다() {
        //given & when & then
        assertThatThrownBy(() -> policy.validate(order, List.of(new CancelProductRequest(null, 1))))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.INVALID_REQUEST);
    }

    @Test
    void quantity가_0보다크지_않으면_에러를_발생시킨다() {
        //given & when & then
        assertThatThrownBy(() -> policy.validate(order, List.of(new CancelProductRequest(101L, 0))))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.INVALID_REQUEST);

        assertThatThrownBy(() -> policy.validate(order, List.of(new CancelProductRequest(101L, -1))))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.INVALID_REQUEST);
    }

    @Test
    void 주문이_주문된상품과_매칭되지_않으면_에러를_발생시킨다() {
        assertThatThrownBy(() -> policy.validate(order, List.of(new CancelProductRequest(999_999L, 1))))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.ORDER_PRODUCT_NOT_BELONGS_TO_ORDER);
    }

    @Test
    void 하나가_CANCELED로_설정되어있으면_주문은_상품취소가_되지_않습니다() {
        // 라인 상태를 CANCELED로 변경
        setField(op1, "status", opCanceled);

        assertThatThrownBy(() -> policy.validate(order, List.of(new CancelProductRequest(101L, 1))))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.ORDER_NOT_CANCELABLE);
    }

    @Test
    void 리뷰까지쓴_경우_취소가_불가하다() {
        setField(op1, "status", opPaidAndReviewCompleted);

        assertThatThrownBy(() -> policy.validate(order, List.of(new CancelProductRequest(101L, 1))))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.ORDER_NOT_CANCELABLE);
    }

    @Test
    void 요청들어온_수량이_기존_수량을_넘으면_재고가맞지_않는_경우이므로_재고부족_오류를_띄운다() {
        // 이미 2개 취소된 상태로 만들어 남은 수량 1로 만듦 (원 수량 3)
        op1.cancelQuantityCheckChange(2, opCanceled, opPartialCanceled); // 내부에서 partial 상태로 바뀔 것

        // 이제 2개를 더 취소하려고 하면 남은 1 초과 → 에러
        assertThatThrownBy(() -> policy.validate(order, List.of(new CancelProductRequest(101L, 2))))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.NOT_ENOUGH_STOCK);
    }

    @Test
    void 동일한_상품에_대해서_합쳐서_요청이_들어오면_확인_전에_합쳐진다() {
        // 동일 라인에 1 + 2 요청 합쳐서 3 (원 수량 3, 취소 이력 0) → 가능
        List<CancelProductRequest> reqs = List.of(
                new CancelProductRequest(101L, 1),
                new CancelProductRequest(101L, 2)
        );

        assertThatCode(() -> policy.validate(order, reqs))
                .doesNotThrowAnyException();
    }



}