package org.example.sansam.payment.controller;

import org.example.sansam.exception.pay.CustomException;
import org.example.sansam.exception.pay.ErrorCode;
import org.example.sansam.payment.dto.*;
import org.example.sansam.payment.service.PaymentCancelService;
import org.example.sansam.payment.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    @Mock
    PaymentService paymentService;
    @Mock
    PaymentCancelService paymentCancelService;
    @InjectMocks
    PaymentController controller;

    private TossPaymentRequest req(long amount) {
        return new TossPaymentRequest("pay_123", "ORD-20250825-0001", amount);
    }


    @Test
    void confirmPayment_성공_200과_응답바디() {
        // given
        TossPaymentResponse resp = TossPaymentResponse.builder()
                .method("카드")
                .totalAmount(20000L)
                .finalAmount(20000L)
                .approvedAt(LocalDateTime.of(2025, 8, 24, 10, 5))
                .build();

        TossPaymentRequest request = req(20000L);
        when(paymentService.confirmPayment(request)).thenReturn(resp);

        // when
        ResponseEntity<?> result = controller.confirmPayment(request);

        // then
        assertThat(result.getStatusCodeValue()).isEqualTo(200);
        assertThat(result.getBody()).isSameAs(resp);

        verify(paymentService, times(1)).confirmPayment(request);
        verifyNoMoreInteractions(paymentService);
        verifyNoInteractions(paymentCancelService);
    }

    @Test
    void confirmPayment_ORDER_NOT_FOUND_예외_그대로전파() {
        TossPaymentRequest request = req(20000L);
        CustomException ex = new CustomException(ErrorCode.ORDER_NOT_FOUND);
        when(paymentService.confirmPayment(request)).thenThrow(ex);

        assertThatThrownBy(() -> controller.confirmPayment(request)).isSameAs(ex);

        verify(paymentService).confirmPayment(request);
        verifyNoMoreInteractions(paymentService);
        verifyNoInteractions(paymentCancelService);
    }

    @Test
    void confirmPayment_ORDER_AND_PAY_NOT_EQUAL_예외_그대로전파() {
        TossPaymentRequest request = req(999L);
        CustomException ex = new CustomException(ErrorCode.ORDER_AND_PAY_NOT_EQUAL);
        when(paymentService.confirmPayment(request)).thenThrow(ex);

        assertThatThrownBy(() -> controller.confirmPayment(request)).isSameAs(ex);
        verify(paymentService).confirmPayment(request);
    }

    @Test
    void confirmPayment_API_FAILED_예외_그대로전파() {
        TossPaymentRequest request = req(20000L);
        when(paymentService.confirmPayment(request)).thenThrow(new CustomException(ErrorCode.API_FAILED));

        assertThatThrownBy(() -> controller.confirmPayment(request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.API_FAILED);

        verify(paymentService).confirmPayment(request);
    }

    @Test
    void confirmPayment_PAYMENT_FAILED_예외_그대로전파() {
        TossPaymentRequest request = req(20000L);
        when(paymentService.confirmPayment(request)).thenThrow(new CustomException(ErrorCode.PAYMENT_FAILED));

        assertThatThrownBy(() -> controller.confirmPayment(request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PAYMENT_FAILED);

        verify(paymentService).confirmPayment(request);
    }

    @Test
    void handlePaymentCancel_성공_200과_응답바디() {
        // given
        PaymentCancelRequest req = new PaymentCancelRequest(
                "ORDER-12345",
                "단순변심",
                List.of(new CancelProductRequest(101L, 2))
        );
        CancelResponse resp = new CancelResponse("취소가 완료되었습니다.");
        when(paymentCancelService.wantToCancel(req)).thenReturn(resp);

        // when
        ResponseEntity<?> result = controller.handlePaymentCancel(req);

        // then
        assertThat(result.getStatusCodeValue()).isEqualTo(200);
        assertThat(result.getBody()).isSameAs(resp);

        verify(paymentCancelService, times(1)).wantToCancel(req);
        verifyNoMoreInteractions(paymentCancelService);
        verifyNoInteractions(paymentService); // 취소 엔드포인트에서는 호출되지 않아야 함
    }
}