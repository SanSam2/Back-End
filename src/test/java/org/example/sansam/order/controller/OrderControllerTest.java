package org.example.sansam.order.controller;

import org.example.sansam.order.dto.OrderWithProductsResponse;
import org.example.sansam.order.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    OrderService orderService;
    @InjectMocks
    OrderController controller;

    @Test
    void 성공일경우_200_페이지반환() {
        long userId = 1L;
        int page = 0, size = 5;
        Page<OrderWithProductsResponse> page1 =
                new PageImpl<>(java.util.Collections.<OrderWithProductsResponse>emptyList(),
                        org.springframework.data.domain.PageRequest.of(page, size),
                        0);
        when(orderService.getAllOrdersByUserId(userId, page, size))
                .thenReturn(page1);

        var res = controller.getOrdersByUserId(userId, page, size);

        assertThat(res.getStatusCodeValue()).isEqualTo(200);
        assertThat(res.getBody()).isSameAs(page1);
    }

    @Test
    void 예외상황_발생_테스트() {
        long userId = 1L;
        int page = 0, size = 5;
        var ex = new org.example.sansam.exception.pay.CustomException(
                org.example.sansam.exception.pay.ErrorCode.NO_USER_ERROR);

        when(orderService.getAllOrdersByUserId(userId, page, size)).thenThrow(ex);

        assertThatThrownBy(
                () -> controller.getOrdersByUserId(userId, page, size)
        ).isSameAs(ex);
    }
}