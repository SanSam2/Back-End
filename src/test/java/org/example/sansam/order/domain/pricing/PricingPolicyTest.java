package org.example.sansam.order.domain.pricing;

import org.example.sansam.order.domain.OrderProduct;
import org.example.sansam.product.domain.Product;
import org.example.sansam.status.domain.Status;
import org.example.sansam.status.domain.StatusEnum;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PricingPolicyTest {

    BasicPricingPolicy policy = new BasicPricingPolicy();

    private static OrderProduct temp(long unitPrice, int qty, int canceledQty) {
        Product product = mock(Product.class);
        Status waiting = new Status(StatusEnum.ORDER_WAITING);

        OrderProduct op = OrderProduct.create(
                product, unitPrice, qty, "M", "BLACK", "url", waiting
        );

        if (canceledQty > 0) {
            Status canceled = new Status(StatusEnum.CANCEL_COMPLETED);
            Status partialCanceled = new Status(StatusEnum.ORDER_PARTIAL_CANCELED);
            op.cancelQuantityCheckChange(canceledQty, canceled, partialCanceled);
        }
        return op;
    }


    @Test
    void 총액은_각_라인의_단가x유효수량_합이다() {
        // 1000*2 + 500*(3-1) + 200*(1-1) = 2000 + 1000 + 0 = 3000
        List<OrderProduct> temps = List.of(
                temp(1000L, 2, 0),
                temp(500L, 3, 1),
                temp(200L, 1, 1)
        );

        long total = policy.totalOf(temps);
        assertThat(total).isEqualTo(3000L);
    }

    @Test
    void orderProduct_없으면_총액은_0이다() {
        assertThat(policy.totalOf(List.of())).isEqualTo(0L);
    }

    @Test
    void 전량취소된_라인은_0으로_계산된다() {
        OrderProduct op = OrderProduct.create(mock(Product.class), 1000L, 5, "M", "BLACK", "url",
                new Status(StatusEnum.ORDER_WAITING));
        op.cancelQuantityCheckChange(5,
                new Status(StatusEnum.CANCEL_COMPLETED),
                new Status(StatusEnum.ORDER_PRODUCT_PARTIALLY_CANCELED));
        assertThat(policy.totalOf(List.of(op))).isEqualTo(0L);
    }

    @Test
    void 큰값_계산도_가능하다() {
        OrderProduct op = OrderProduct.create(mock(Product.class), 1_000_000L, 2_000, "M","BLACK","url",
                new Status(StatusEnum.ORDER_WAITING));
        assertThat(policy.totalOf(List.of(op))).isEqualTo(2_000_000_000L);
    }

    @Test
    void 단가가_null이면_0으로_계산된다() {
        OrderProduct op = mock(OrderProduct.class);
        // null 반환하도록 스텁
        when(op.getOrderedProductPrice()).thenReturn(null);
        when(op.getQuantity()).thenReturn(3);
        when(op.getCanceledQuantity()).thenReturn(1);

        long total = policy.totalOf(List.of(op));

        assertThat(total).isZero();
    }

}