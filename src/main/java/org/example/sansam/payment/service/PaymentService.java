package org.example.sansam.payment.service;

import lombok.RequiredArgsConstructor;
import org.example.sansam.exception.pay.CustomException;
import org.example.sansam.exception.pay.ErrorCode;
import org.example.sansam.order.domain.Order;
import org.example.sansam.order.repository.OrderRepository;

import org.example.sansam.payment.domain.PaymentMethodType;
import org.example.sansam.payment.domain.Payments;
import org.example.sansam.payment.domain.PaymentsType;
import org.example.sansam.payment.dto.TossPaymentRequest;
import org.example.sansam.payment.repository.PaymentsRepository;
import org.example.sansam.payment.repository.PaymentsTypeRepository;
import org.example.sansam.status.domain.Status;
import org.example.sansam.status.domain.StatusEnum;
import org.example.sansam.status.repository.StatusRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final OrderRepository orderRepository;

    private final PaymentsTypeRepository paymentsTypeRepository;
    private final PaymentsRepository paymentsRepository;
    private final StatusRepository statusRepository;

    private final PaymentApiClient paymentApiClient;

    @Transactional
    public void confirmPayment(TossPaymentRequest request) throws Exception {
        try {
            // 주문 상태 업데이트(강결합이 과연 좋을까? 단점이 뭐가 있을까?) -> 결국 분리를 해야한다
            Order order = orderRepository.findByOrderNumber(request.getOrderId())
                    .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));
            order.addPaymentKey(request.getPaymentKey()); //더티체킹 일어날텐데 왜 굳이 밑에 save가 있나요?

            Map<String, Object> response = paymentApiClient.confirmPayment(request);
            String method = (String) response.get("method");
            savePaymentInfo(method, request);


            Status orderPaid = statusRepository.findByStatusName(StatusEnum.ORDER_PAID);
            Status orderProductPaid = statusRepository.findByStatusName(StatusEnum.ORDER_PRODUCT_PAID_AND_REVIEW_REQUIRED);
            order.completePayment(orderPaid, orderProductPaid, request.getPaymentKey());
            orderRepository.save(order);

        } catch (Exception e) {
            throw new CustomException(ErrorCode.PAYMENT_CONFIRM_FAILED);
        }
    }

    private void savePaymentInfo(String methodKorean, TossPaymentRequest request) {
        // 주문 정보 조회(이거뭐죠??)
        Order order = orderRepository.findByOrderNumber(request.getOrderId())
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        PaymentMethodType paymentMethodType = PaymentMethodType.fromKorean(methodKorean);
        PaymentsType paymentsType = findPaymentsType(paymentMethodType);
        Long totalPrice = request.getAmount();

        // Payments 엔티티 생성과 저장
        Payments payment = Payments.create(order,paymentsType,totalPrice, LocalDateTime.now());
        paymentsRepository.save(payment);
    }

    private PaymentsType findPaymentsType(PaymentMethodType paymentMethodType){
        return paymentsTypeRepository.findByTypeName(paymentMethodType)
                .orElseThrow(()-> new CustomException(ErrorCode.UNSUPPORTED_PAYMENT_METHOD)); // restControllerAdvice
    }
}
