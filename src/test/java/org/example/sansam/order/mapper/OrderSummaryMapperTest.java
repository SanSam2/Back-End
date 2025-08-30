package org.example.sansam.order.mapper;

import org.example.sansam.order.domain.Order;
import org.example.sansam.order.domain.OrderProduct;
import org. example.sansam.order.dto.OrderWithProductsResponse.ProductSummary;
import org.example.sansam.order.dto.OrderWithProductsResponse;
import org.example.sansam.product.domain.Product;
import org.example.sansam.status.domain.Status;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class OrderSummaryMapperTest {

    private final OrderSummaryMapper mapper = Mappers.getMapper(OrderSummaryMapper.class);


    @Test
    void toOrderDto_null이면_null반환() {
        OrderWithProductsResponse dto = mapper.toOrderDto(null);
        assertThat(dto).isNull();
    }

    @Test
    void toProductSummary_null이면_null반환() {
        ProductSummary ps = mapper.toProductSummary(null);
        assertThat(ps).isNull();
    }

    @Test
    void toOrderDto_order가_null이면_null반환() {
        assertThat(mapper.toOrderDto(null)).isNull();
    }

    @Test
    void toOrderDto_status가_null이면_status필드_null로_매핑된다() {
        Order order = mock(Order.class);
        when(order.getOrderNumber()).thenReturn("ORD-1");
        when(order.getTotalAmount()).thenReturn(1000L);
        when(order.getCreatedAt()).thenReturn(LocalDateTime.now());
        when(order.getStatus()).thenReturn(null);
        when(order.getOrderProducts()).thenReturn(List.of());

        OrderWithProductsResponse dto = mapper.toOrderDto(order);
        assertThat(dto).isNotNull();
        assertThat(dto.getOrderStatus()).isNull();
    }

    @Test
    void toOrderDto_orderProducts가_null이면_items필드_null로_매핑된다() {
        Order order = mock(Order.class);
        when(order.getOrderNumber()).thenReturn("ORD-2");
        when(order.getTotalAmount()).thenReturn(0L);
        when(order.getCreatedAt()).thenReturn(LocalDateTime.now());
        when(order.getStatus()).thenReturn(new Status(null));
        when(order.getOrderProducts()).thenReturn(null);

        OrderWithProductsResponse dto = mapper.toOrderDto(order);
        assertThat(dto).isNotNull();
        assertThat(dto.getItems()).isNull();
    }

    @Test
    void toProductSummary_product가_null이면_productId와_name이_null로_매핑된다() {
        OrderProduct op = mock(OrderProduct.class);
        when(op.getProduct()).thenReturn(null);
        when(op.getStatus()).thenReturn(new Status(null));
        when(op.getQuantity()).thenReturn(3);

        OrderWithProductsResponse.ProductSummary ps = mapper.toProductSummary(op);
        assertThat(ps).isNotNull();

        assertThat(ps.getProductId()).isNull();
        assertThat(ps.getProductName()).isNull();
    }

    @Test
    void toProductSummary_status가_null이면_status필드_null() {
        Product product = mock(Product.class);
        when(product.getId()).thenReturn(10L);
        when(product.getProductName()).thenReturn("양말");

        OrderProduct op = mock(OrderProduct.class);
        when(op.getProduct()).thenReturn(product);
        when(op.getStatus()).thenReturn(null);
        when(op.getQuantity()).thenReturn(1);

        OrderWithProductsResponse.ProductSummary ps = mapper.toProductSummary(op);
        assertThat(ps).isNotNull();
        assertThat(ps.getOrderProductStatus()).isNull();
    }

}