package org.example.sansam.payment.service;

import lombok.RequiredArgsConstructor;

import org.example.sansam.exception.pay.CustomException;
import org.example.sansam.exception.pay.ErrorCode;
import org.example.sansam.order.domain.Order;
import org.example.sansam.order.domain.OrderProduct;
import org.example.sansam.payment.adapter.CancelResponseNormalize;
import org.example.sansam.payment.util.IdempotencyKeyUtil;
import org.example.sansam.payment.dto.CancelResponse;
import org.example.sansam.payment.policy.CancellationPolicy;
import org.example.sansam.order.repository.OrderRepository;
import org.example.sansam.payment.dto.CancelProductRequest;
import org.example.sansam.payment.dto.PaymentCancelRequest;
import org.example.sansam.payment.repository.PaymentsRepository;
import org.example.sansam.status.repository.StatusRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;


import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentCancelService {

    private final PaymentApiClient paymentApiClient;
    private final AfterConfirmTransactionService afterConfirmTransactionService;

    private final OrderRepository orderRepository;
    private final CancellationPolicy cancellationPolicy;
    private final CancelResponseNormalize normalizeResponse;


    @Transactional
    public CancelResponse wantToCancel(PaymentCancelRequest request) {

        //컨트롤러 응답값 방어
        if (request == null || request.getItems() == null || request.getItems().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        //order 검증(policy를 통해, 존재하는지, 주문했던 재고에 맞게 취소하는건지, 상태가 취소할 수 있는 상태인지 (주문과 주문상태 둘다))
        Order cancelOrder = orderRepository.findOrderByOrderNumber(request.getOrderId());
        List<CancelProductRequest> productRequests = request.getItems();
        cancellationPolicy.validate(cancelOrder, productRequests);

        //토스에 보낼 정보 구하기 (취소 총 가격 + paymentKey)
        Map<Long, Long> unitPriceByOpId = cancelOrder.getOrderProducts().stream()
                .collect(Collectors.toMap(
                        OrderProduct::getId,
                        op -> op.getOrderedProductPrice()
                ));

        long cancelTotalAmount = request.getItems().stream()
                .mapToLong(i -> {
                    Long opId = i.getOrderProductId();
                    if (opId == null)
                        throw new CustomException(ErrorCode.INVALID_REQUEST);
                    Long unit = unitPriceByOpId.get(opId);
                    if (unit == null)
                        throw new CustomException(ErrorCode.ORDER_PRODUCT_NOT_BELONGS_TO_ORDER);
                    return Math.multiplyExact(unit, (long) i.getCancelQuantity());
                })
                .sum();

        String paymentKey = Optional.ofNullable(cancelOrder.getPaymentKey())
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENTS_NOT_FOUND));

        String idempotencyKey = IdempotencyKeyUtil.forCancel(
                paymentKey,
                cancelTotalAmount,
                request.getCancelReason()
        );


        //토스로 취소 요청 보내기
        Map<String, Object> cancelResult = paymentApiClient.tossPaymentCancel(paymentKey ,cancelTotalAmount, request.getCancelReason(),idempotencyKey);
        CancelResponseNormalize.ParsedCancel parsed = normalizeResponse.parseTossCancelResponse(cancelResult);

        return afterConfirmTransactionService.saveCancellation(cancelOrder,parsed,request,idempotencyKey);
    }

}
