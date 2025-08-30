package org.example.sansam.order.domain;

import org.example.sansam.exception.pay.CustomException;
import org.example.sansam.exception.pay.ErrorCode;
import org.example.sansam.order.domain.nameformatter.KoreanOrdernameFormatter;
import org.example.sansam.order.domain.nameformatter.OrderNameFormatter;
import org.example.sansam.order.domain.ordernumber.OrderNumberPolicy;
import org.example.sansam.order.domain.pricing.PricingPolicy;
import org.example.sansam.product.domain.Product;
import org.example.sansam.status.domain.Status;
import org.example.sansam.status.domain.StatusEnum;
import org.example.sansam.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class OrderTest {

    private Status orderWaitingStatus;
    private User testUser;

    private User stubUser(long id) {
        User u = mock(User.class);
        given(u.getId()).willReturn(id);
        return u;
    }

    private static class fakePolicy implements PricingPolicy {
        List<OrderProduct> captured;
        long toReturn;
        fakePolicy(long toReturn) {
            this.toReturn = toReturn;
        }

        @Override
        public Long totalOf(List<OrderProduct> products) {
            this.captured = products;
            return toReturn;
        }
    }

    private static class fakeOrderNumberPolicy implements OrderNumberPolicy {

        @Override
        public String makeOrderNumber() {
            return "1234567890-123e4567-e89b-12d3-a456-426614174000";
        }
    }

    private static void setPrivateList(Object target, String fieldName, List<?> value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }

    private static void set(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }

    @BeforeEach
    void setUp() {
        orderWaitingStatus = new Status(StatusEnum.ORDER_WAITING);
        testUser = stubUser(1L);
    }


    private OrderProduct temp(String productName, long unitPrice, int qty) {
        var product = mock(org.example.sansam.product.domain.Product.class);
        given(product.getProductName()).willReturn(productName);

        return OrderProduct.create(product, unitPrice, qty, "M", "BLACK", "url", orderWaitingStatus);
    }

    @Test
    void create로_Order객체_생성할_수_있다() {
        //given
        //when
        fakeOrderNumberPolicy fakeOrderNumberPolicy = new fakeOrderNumberPolicy();
        Order order = Order.create(testUser, orderWaitingStatus,fakeOrderNumberPolicy, LocalDateTime.now());

        //then
        assertThat(order.getUser().getId()).isEqualTo(testUser.getId());
        assertThat(order.getStatus().getStatusName()).isEqualTo(orderWaitingStatus.getStatusName());
    }

    @Test
    void addOrderProduct로_OrderProduct를_Order에_붙일_수_있다() {
        // given
        fakeOrderNumberPolicy fakeOrderNumberPolicy = new fakeOrderNumberPolicy();
        Order order = Order.create(testUser, orderWaitingStatus,fakeOrderNumberPolicy, LocalDateTime.now());
        Product product = mock(Product.class);
        Status opWaiting = new Status(StatusEnum.ORDER_PRODUCT_WAITING);

        OrderProduct orderProduct = OrderProduct.create(product, 10000L, 2, "M",
                "BLACK", "url", opWaiting);

        // when
        order.addOrderProduct(orderProduct);

        // then
        assertThat(order.getOrderProducts()).containsExactly(orderProduct);
    }

    @Test
    void addOrderName으로_한줄짜리_주문명을_생성할_수_있담() {
        //given
        fakeOrderNumberPolicy fakeOrderNumberPolicy = new fakeOrderNumberPolicy();
        Order order = Order.create(testUser, orderWaitingStatus,fakeOrderNumberPolicy, LocalDateTime.now());
        order.addOrderProduct(temp("프라다 반팔", 10000L, 1));

        //when
        order.addOrderName(KoreanOrdernameFormatter.INSTANCE);

        //then
        assertThat(order.getOrderName()).isEqualTo("프라다 반팔");
    }

    @Test
    void addOrderName으로_여러상품의_주문명을_생성할_수_있담(){
        //given
        fakeOrderNumberPolicy fakeOrderNumberPolicy = new fakeOrderNumberPolicy();
        Order order = Order.create(testUser, orderWaitingStatus,fakeOrderNumberPolicy, LocalDateTime.now());
        order.addOrderProduct(temp("프라다 반팔", 10000L, 1));
        order.addOrderProduct(temp("프라다 긴팔", 20000L, 1));
        order.addOrderProduct(temp("셔츠", 15000L, 1));


        //when
        order.addOrderName(KoreanOrdernameFormatter.INSTANCE);

        //then
        assertThat(order.getOrderName()).isEqualTo("프라다 반팔 외 2건");
    }

    @Test
    void orderProduct가_없으면_addOrderName에서_에러가_발생한다(){
        //given
        fakeOrderNumberPolicy fakeOrderNumberPolicy = new fakeOrderNumberPolicy();
        Order order = Order.create(testUser, orderWaitingStatus,fakeOrderNumberPolicy, LocalDateTime.now());

        //when, then
        assertThatThrownBy(() -> order.addOrderName(KoreanOrdernameFormatter.INSTANCE))
                .isInstanceOf(org.example.sansam.exception.pay.CustomException.class)
                .hasMessageContaining("주문에 상품이 없습니다.");
    }

    @Test
    void addOrderName에_전달된_Formatter가_실제로_사용된다(){
        //given
        OrderNameFormatter formatter = (first, others) -> first + "->" + others;
        fakeOrderNumberPolicy fakeOrderNumberPolicy = new fakeOrderNumberPolicy();
        Order order = Order.create(testUser, orderWaitingStatus,fakeOrderNumberPolicy, LocalDateTime.now());
        order.addOrderProduct(temp("티셔츠", 10000L, 1));
        order.addOrderProduct(temp("셔츠", 15000L, 1));

        //when
        order.addOrderName(formatter);

        //then
        assertThat(order.getOrderName()).isEqualTo("티셔츠->1");
    }


    @Test
    void calcTotal_정책반환값을_totalAmount에_세팅한다() {
        // given
        fakeOrderNumberPolicy fakeOrderNumberPolicy = new fakeOrderNumberPolicy();
        Order order = Order.create(testUser, orderWaitingStatus,fakeOrderNumberPolicy, LocalDateTime.now());
        order.addOrderProduct(temp("티셔츠", 10000L, 2));
        order.addOrderProduct(temp("치마", 100L, 1));

        fakePolicy policy = new fakePolicy(9999L);

        // when
        order.calcTotal(policy);

        // then
        assertThat(order.getTotalAmount()).isEqualTo(9999L);
        assertThat(policy.captured).isSameAs(order.getOrderProducts()); // 같은 리스트 객체를 넘겼는지
    }

    @Test
    void calcTotal_여러번_호출해도_마지막_정책결과로_갱신된다() {
        fakeOrderNumberPolicy fakeOrderNumberPolicy = new fakeOrderNumberPolicy();
        Order order = Order.create(testUser, orderWaitingStatus,fakeOrderNumberPolicy, LocalDateTime.now());
        order.addOrderProduct(temp("티셔츠", 10000L, 2));

        order.calcTotal(new fakePolicy(100L));
        assertThat(order.getTotalAmount()).isEqualTo(100L);

        order.calcTotal(new fakePolicy(42L));
        assertThat(order.getTotalAmount()).isEqualTo(42L);
    }

    @Test
    void addOrderNumber를_통해서_OrderNumber를_추가할_수_있다(){
        //given
        fakeOrderNumberPolicy fakeOrderNumberPolicy = new fakeOrderNumberPolicy();
        Order order = Order.create(testUser, orderWaitingStatus,fakeOrderNumberPolicy, LocalDateTime.now());

        //when
        order.addOrderNumber(fakeOrderNumberPolicy);

        //then
        assertThat(order.getOrderNumber()).isNotNull();
        assertThat(order.getOrderNumber()).isNotBlank();
        assertThat(order.getOrderNumber()).isEqualTo("1234567890-123e4567-e89b-12d3-a456-426614174000");
    }

    @Test
    void completePayment를_통한_주문상태변경_및_모든상품_상태업데이트(){
        //given
        fakeOrderNumberPolicy fakeOrderNumberPolicy = new fakeOrderNumberPolicy();
        Order order = Order.create(testUser, orderWaitingStatus,fakeOrderNumberPolicy, LocalDateTime.now());
        order.addOrderProduct(temp("티셔츠", 10000L, 2));
        order.addOrderProduct(temp("치마", 20000L, 2));
        Status orderPaidStatus = new Status(StatusEnum.ORDER_PAID);
        Status orderProductPaid = new Status(StatusEnum.ORDER_PRODUCT_PAID_AND_REVIEW_REQUIRED);


        //when
        order.changeStatusWhenCompletePayment(orderPaidStatus,orderProductPaid);

        //then
        assertThat(order.getStatus().getStatusName()).isEqualTo(orderPaidStatus.getStatusName());
        assertThat(order.getOrderProducts().get(0).getStatus().getStatusName()).isEqualTo(orderProductPaid.getStatusName());
        assertThat(order.getOrderProducts().get(1).getStatus().getStatusName()).isEqualTo(orderProductPaid.getStatusName());
    }

    @Test
    void completePayment호출시_상품없으면_CustomException_예외처리() throws Exception{
        // given
        fakeOrderNumberPolicy fakeOrderNumberPolicy = new fakeOrderNumberPolicy();
        Order order = Order.create(testUser, orderWaitingStatus,fakeOrderNumberPolicy, LocalDateTime.now());
        Status orderPaidStatus = new Status(StatusEnum.ORDER_PAID);
        Status orderProductPaid = new Status(StatusEnum.ORDER_PRODUCT_PAID_AND_REVIEW_REQUIRED);
        // when
        CustomException ex = assertThrows(CustomException.class, () -> order.changeStatusWhenCompletePayment(orderPaidStatus,orderProductPaid));
        // then
        assertEquals(ErrorCode.NO_ITEM_IN_ORDER,ex.getErrorCode());
        assertSame(orderWaitingStatus, order.getStatus());
    }

    @Test
    void 상품없으면_예외_NO_ITEM_IN_ORDER_상태변경없음() throws Exception {
        //given
        Order order = new Order();
        Status orderAllCanceledStatus = new Status(StatusEnum.ORDER_ALL_CANCELED);
        Status orderPartiallyCanceledStatus = new Status(StatusEnum.ORDER_PARTIAL_CANCELED);
        set(order, "status", orderWaitingStatus);
        set(order, "orderProducts", new ArrayList<OrderProduct>());

        //when
        CustomException ex = assertThrows(CustomException.class,
                () -> order.changeStatusAfterItemCancellation(orderAllCanceledStatus, orderPartiallyCanceledStatus));

        //then
        assertEquals(ErrorCode.NO_ITEM_IN_ORDER, ex.getErrorCode());
        assertSame(orderWaitingStatus, order.getStatus());
    }

    @Test
    void 전부취소면_ORDER_ALL_CANCELED로_변경() throws Exception {
        //given
        Order order = new Order();
        set(order, "status", orderWaitingStatus);
        OrderProduct op1 = mock(OrderProduct.class);
        OrderProduct op2 = mock(OrderProduct.class);
        when(op1.getStatus()).thenReturn(new Status(StatusEnum.ORDER_PRODUCT_CANCELED));
        when(op2.getStatus()).thenReturn(new Status(StatusEnum.ORDER_PRODUCT_CANCELED));
        set(order, "orderProducts", new ArrayList<>(List.of(op1, op2)));
        Status orderAllCanceledStatus = new Status(StatusEnum.ORDER_ALL_CANCELED);
        Status orderPartiallyCanceledStatus = new Status(StatusEnum.ORDER_PARTIAL_CANCELED);

        //when
        order.changeStatusAfterItemCancellation(orderAllCanceledStatus,orderPartiallyCanceledStatus);

        //then
        assertEquals(StatusEnum.ORDER_ALL_CANCELED, order.getStatus().getStatusName());
    }

    @Test
    void 일부만취소면_ORDER_PARTIAL_CANCELED로_변경() throws Exception {
        //given
        Order order = new Order();
        set(order, "status", new Status(StatusEnum.ORDER_WAITING));
        OrderProduct op1 = mock(OrderProduct.class);
        OrderProduct op2 = mock(OrderProduct.class);
        when(op1.getStatus()).thenReturn(new Status(StatusEnum.ORDER_PRODUCT_CANCELED));
        when(op2.getStatus()).thenReturn(new Status(StatusEnum.ORDER_PRODUCT_PAID_AND_REVIEW_REQUIRED));
        set(order, "orderProducts", new ArrayList<>(List.of(op1, op2)));
        Status orderAllCanceledStatus = new Status(StatusEnum.ORDER_ALL_CANCELED);
        Status orderPartiallyCanceledStatus = new Status(StatusEnum.ORDER_PARTIAL_CANCELED);

        //when
        order.changeStatusAfterItemCancellation(orderAllCanceledStatus,orderPartiallyCanceledStatus);

        //then
        assertEquals(StatusEnum.ORDER_PARTIAL_CANCELED, order.getStatus().getStatusName());
    }

    @Test
    void 취소없는경우_상태변경없음() throws Exception {
        //given
        Order order = new Order();
        set(order, "status", orderWaitingStatus);
        OrderProduct op1 = mock(OrderProduct.class);
        OrderProduct op2 = mock(OrderProduct.class);
        when(op1.getStatus()).thenReturn(new Status(StatusEnum.ORDER_PRODUCT_PAID_AND_REVIEW_REQUIRED));
        when(op2.getStatus()).thenReturn(new Status(StatusEnum.ORDER_PRODUCT_PAID_AND_REVIEW_REQUIRED));
        set(order, "orderProducts", new ArrayList<>(List.of(op1, op2)));
        Status orderAllCanceledStatus = new Status(StatusEnum.ORDER_ALL_CANCELED);
        Status orderPartiallyCanceledStatus = new Status(StatusEnum.ORDER_PARTIAL_CANCELED);

        //when
        order.changeStatusAfterItemCancellation(orderAllCanceledStatus,orderPartiallyCanceledStatus);

        //then
        assertSame(orderWaitingStatus, order.getStatus());
    }


    @Test
    void getter로_지정한값을_그대로_반환한다() throws Exception {
        //given
        Order order = new Order();
        LocalDateTime now = LocalDateTime.of(2025, 8, 20, 12, 34, 56);
        User user = mock(User.class);

        set(order, "id", 1L);
        set(order, "user", user);
        set(order, "orderName", "테스트주문");
        set(order, "orderNumber", "20250820123456-deadbeef");
        set(order, "totalAmount", 12345L);
        set(order, "createdAt", now);
        set(order, "status", orderWaitingStatus);

        //when&then
        assertEquals(1L, order.getId());
        assertSame(user, order.getUser());
        assertEquals("테스트주문", order.getOrderName());
        assertEquals("20250820123456-deadbeef", order.getOrderNumber());
        assertEquals(12345L, order.getTotalAmount());
        assertEquals(now, order.getCreatedAt());
        assertSame(orderWaitingStatus, order.getStatus());
    }

    @Test
    void orderProducts_기본값은_빈리스트이며_널이아니다_수정가능하다() {
        //given
        Order order = new Order();
        //when&then
        assertNotNull(order.getOrderProducts());
        assertTrue(order.getOrderProducts().isEmpty());
    }

    @Test
    void orderProducts는_수정이_가능하다(){
        //given
        Order order = new Order();
        //given
        OrderProduct op = mock(OrderProduct.class);
        //when
        order.getOrderProducts().add(op);
        //then
        assertEquals(1, order.getOrderProducts().size());
    }

    @Test
    void 상품목록_null이면_예외_NO_ITEM_IN_ORDER_상태변경없음() throws Exception {
        // given
        Order order = new Order();
        set(order, "status", orderWaitingStatus);
        set(order, "orderProducts", null); // ← null 분기 타게

        Status allCanceled = new Status(StatusEnum.ORDER_ALL_CANCELED);
        Status partialCanceled = new Status(StatusEnum.ORDER_PARTIAL_CANCELED);

        // when
        CustomException ex = assertThrows(CustomException.class,
                () -> order.changeStatusAfterItemCancellation(allCanceled, partialCanceled));

        // then
        assertEquals(ErrorCode.NO_ITEM_IN_ORDER, ex.getErrorCode());
        assertSame(orderWaitingStatus, order.getStatus());
    }

    @Test
    void areAllItemsCanceled_빈리스트면_false() throws Exception {
        Order order = new Order();
        set(order, "orderProducts", new ArrayList<OrderProduct>()); // 빈 리스트

        var m = Order.class.getDeclaredMethod("areAllItemsCanceled");
        m.setAccessible(true);
        boolean result = (boolean) m.invoke(order);

        assertFalse(result);
    }

    @Test
    void areAllItemsCanceled_전부취소면_true() throws Exception {
        Order order = new Order();
        OrderProduct op1 = mock(OrderProduct.class);
        OrderProduct op2 = mock(OrderProduct.class);
        when(op1.getStatus()).thenReturn(new Status(StatusEnum.ORDER_PRODUCT_CANCELED));
        when(op2.getStatus()).thenReturn(new Status(StatusEnum.ORDER_PRODUCT_CANCELED));
        set(order, "orderProducts", new ArrayList<>(List.of(op1, op2)));

        var m = Order.class.getDeclaredMethod("areAllItemsCanceled");
        m.setAccessible(true);
        boolean result = (boolean) m.invoke(order);

        assertTrue(result);
    }

}