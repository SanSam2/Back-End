package org.example.sansam.order.controller;

import org.example.sansam.exception.pay.CustomException;
import org.example.sansam.exception.pay.ErrorCode;
import org.example.sansam.order.dto.OrderRequest;
import org.example.sansam.order.dto.OrderResponse;
import org.example.sansam.order.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderRestControllerTest {

    @Mock
    OrderService orderService;
    @InjectMocks
    OrderRestController controller;

    @Test
    void saveOrder로_정상200과_응답바디를_반환한다() {
        //given
        OrderRequest req = mock(OrderRequest.class);
        OrderResponse res = mock(OrderResponse.class);
        when(orderService.saveOrder(req)).thenReturn(res);

        //when
        ResponseEntity<?> result = controller.saveOrder(req);

        //then
        assertThat(result.getStatusCodeValue()).isEqualTo(200);
        assertThat(result.getBody()).isSameAs(res);
        verify(orderService, times(1)).saveOrder(req);
        verifyNoMoreInteractions(orderService);
    }

    @Test
    void saveOrder에서_예외를_그대로_빼준다(){
        //given
        OrderRequest req = mock(OrderRequest.class);
        CustomException ex = new CustomException(ErrorCode.NOT_ENOUGH_STOCK);
        when(orderService.saveOrder(req)).thenThrow(ex);

        //when&then
        assertThatThrownBy(() -> controller.saveOrder(req))
                .isSameAs(ex);

        verify(orderService, times(1)).saveOrder(req);
        verifyNoMoreInteractions(orderService);
    }

}