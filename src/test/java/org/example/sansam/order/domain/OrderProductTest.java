package org.example.sansam.order.domain;

import org.example.sansam.exception.pay.CustomException;
import org.example.sansam.exception.pay.ErrorCode;
import org.example.sansam.product.domain.Product;
import org.example.sansam.status.domain.Status;
import org.example.sansam.status.domain.StatusEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class OrderProductTest {

    private Product product;
    private Status waiting;
    private Status canceled;
    private Status partialCanceled;
    private Status paidReviewRequired;
    private String defaultSize;
    private String defaultColor;
    private String defaultRepUrl;
    private long defaultPrice;

    @BeforeEach
    void setUp() {
        product = mock(Product.class);
        waiting = new Status(StatusEnum.ORDER_PRODUCT_WAITING);
        canceled = new Status(StatusEnum.ORDER_PRODUCT_CANCELED);
        partialCanceled = new Status(StatusEnum.ORDER_PRODUCT_PARTIALLY_CANCELED);
        paidReviewRequired = new Status(StatusEnum.ORDER_PRODUCT_PAID_AND_REVIEW_REQUIRED);

        defaultSize = "M";
        defaultColor = "BLACK";
        defaultRepUrl = "https://img.test/item.png";
        defaultPrice = 1_000L;
    }

    private OrderProduct newOP(int qty) {
        return OrderProduct.create(
                product, defaultPrice, qty, defaultSize, defaultColor, defaultRepUrl, waiting
        );
    }

    @Test
    void create_필드들이_정상세팅된다() {
        // given
        long price = 12_345L;
        int qty = 2;
        String size = "M";
        String color = "RED";
        String repUrl = "https://img.test/item.png";
        Status status = new Status(StatusEnum.ORDER_PRODUCT_WAITING);

        // when
        OrderProduct op = OrderProduct.create(product, price, qty, size, color, repUrl, status);

        // then
        assertThat(op.getProduct()).isSameAs(product);
        assertThat(op.getOrderedProductPrice()).isEqualTo(price);
        assertThat(op.getQuantity()).isEqualTo(qty);
        assertThat(op.getOrderedProductSize()).isEqualTo(size);
        assertThat(op.getOrderedProductColor()).isEqualTo(color);
        assertThat(op.getRepresentativeURL()).isEqualTo(repUrl);
        assertThat(op.getStatus()).isSameAs(status);

        // 기본 취소수량 0
        assertThat(op.getCanceledQuantity()).isNotNull().isZero();
    }

    @Test
    void cancelQuantity_0_이하이면_예외() {
        //given
        OrderProduct op = newOP(1);

        //when & then
        CustomException ex1 = assertThrows(CustomException.class,
                () -> op.cancelQuantityCheckChange(0, canceled, partialCanceled));
        assertThat(ex1.getErrorCode()).isEqualTo(ErrorCode.CANCEL_QUANTITY_MUST_MORE_THEN_ZERO);

        CustomException ex2 = assertThrows(CustomException.class,
                () -> op.cancelQuantityCheckChange(-1, canceled, partialCanceled));
        assertThat(ex2.getErrorCode()).isEqualTo(ErrorCode.CANCEL_QUANTITY_MUST_MORE_THEN_ZERO);
    }

    @Test
    void cancelQuantity_전체취소면_상태가_취소가_된다() {
        //given
        OrderProduct op = newOP(3);

        //when
        op.cancelQuantityCheckChange(3, canceled, partialCanceled);

        //then
        assertThat(op.getStatus().getStatusName()).isEqualTo(StatusEnum.ORDER_PRODUCT_CANCELED);
    }

    @Test
    void cancelQuantity_일부취소면_상태가_부분취소가_된다() {
        //given
        OrderProduct op = newOP(5);

        //when
        op.cancelQuantityCheckChange(2, canceled, partialCanceled);

        //then
        assertThat(op.getStatus().getStatusName()).isEqualTo(StatusEnum.ORDER_PRODUCT_PARTIALLY_CANCELED);
    }
}
