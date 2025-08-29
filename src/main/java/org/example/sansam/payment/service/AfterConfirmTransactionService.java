package org.example.sansam.payment.service;


import lombok.RequiredArgsConstructor;
import org.example.sansam.exception.pay.CustomException;
import org.example.sansam.exception.pay.ErrorCode;
import org.example.sansam.notification.event.PaymentCompleteEmailEvent;
import org.example.sansam.notification.event.PaymentCompleteEvent;
import org.example.sansam.order.domain.Order;
import org.example.sansam.order.domain.OrderProduct;
import org.example.sansam.payment.Mapper.PaymentMapper;
import org.example.sansam.payment.adapter.CancelResponseNormalize;
import org.example.sansam.payment.domain.PaymentCancellation;
import org.example.sansam.payment.domain.PaymentCancellationHistory;
import org.example.sansam.payment.domain.Payments;
import org.example.sansam.payment.dto.CancelProductRequest;
import org.example.sansam.payment.dto.CancelResponse;
import org.example.sansam.payment.dto.PaymentCancelRequest;
import org.example.sansam.payment.dto.TossPaymentResponse;
import org.example.sansam.payment.repository.PaymentsCancelRepository;
import org.example.sansam.payment.repository.PaymentsRepository;
import org.example.sansam.status.domain.Status;
import org.example.sansam.status.domain.StatusEnum;
import org.example.sansam.status.repository.StatusRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.example.sansam.payment.adapter.TossApprovalNormalizer.Normalized;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AfterConfirmTransactionService {
    private final PaymentsRepository paymentsRepository;
    private final StatusRepository statusRepository;
    private final PaymentMapper paymentMapper;
    private final ApplicationEventPublisher eventPublisher;


    private final PaymentsCancelRepository paymentsCancelRepository;

    @Transactional
    public TossPaymentResponse approveInTransaction(Order order, Normalized normalizePayment) {
        // 멱등성보장 : 같은 paymentKey면 기존 결과 반환
        Payments existing = paymentsRepository.findByPaymentKey(normalizePayment.paymentKey()).orElse(null);
        if (existing != null)
            return paymentMapper.toTossPaymentResponse(existing);

        Status paymentComplete = statusRepository.findByStatusName(StatusEnum.PAYMENT_COMPLETED);

        // 결제 엔티티 생성 & 영속화
        Payments payment = paymentsRepository.save(
                Payments.create(order, normalizePayment.paymentsType(), normalizePayment.paymentKey(),
                        normalizePayment.totalAmount(), normalizePayment.balanceAmount(),
                        normalizePayment.requestedAtKst(), normalizePayment.approvedAtKst(),paymentComplete)
        );

        order.addPaymentKey(normalizePayment.paymentKey());

        // 주문 상태 전이 (결제 승인 완료만-> 애초에 null일 수가 없음)
        Status orderPaid = statusRepository.findByStatusName(StatusEnum.ORDER_PAID);
        Status opPaid    = statusRepository.findByStatusName(StatusEnum.ORDER_PRODUCT_PAID_AND_REVIEW_REQUIRED);
        order.changeStatusWhenCompletePayment(orderPaid, opPaid);

        // 현재 트랜잭션 안에서 발행되고 있음,...Listener가 afterCommit이어야함
        eventPublisher.publishEvent(new PaymentCompleteEmailEvent(order.getUser(), order.getOrderName(), order.getTotalAmount()));
        eventPublisher.publishEvent(new PaymentCompleteEvent(order.getUser(), order.getOrderName(), order.getTotalAmount()));

        return paymentMapper.toTossPaymentResponse(payment);
    }

    @Transactional
    public CancelResponse saveCancellation (Order cancelOrder, CancelResponseNormalize.ParsedCancel parsed, PaymentCancelRequest request, String idempotencyKey){
        //TODO: 상태값이 주기적으로 변화하지않는 값이라면 DB 부하를 줄여보자.(ex. 캐싱)
        Status orderAllCanceled = statusRepository.findByStatusName(StatusEnum.ORDER_ALL_CANCELED);
        Status orderPartialCanceled = statusRepository.findByStatusName(StatusEnum.ORDER_PARTIAL_CANCELED);
        Status cancelCompleted = statusRepository.findByStatusName(StatusEnum.CANCEL_COMPLETED);
        Status orderProductCanceled = statusRepository.findByStatusName(StatusEnum.ORDER_PRODUCT_CANCELED);
        Status orderProductPartiallyCanceled = statusRepository.findByStatusName(StatusEnum.ORDER_PRODUCT_PARTIALLY_CANCELED);

        PaymentCancellation pc = PaymentCancellation.create(
                parsed.paymentKey(),
                parsed.refundPrice(),
                parsed.cancelReason(),
                idempotencyKey,
                cancelOrder.getId(),
                parsed.canceledAt()
        );

        Map<Long, OrderProduct> opById = cancelOrder.getOrderProducts().stream()
                .collect(Collectors.toMap(OrderProduct::getId, Function.identity()));

        for (CancelProductRequest item : request.getItems()) {
            PaymentCancellationHistory h = PaymentCancellationHistory.create(
                    item.getOrderProductId(), item.getCancelQuantity(), cancelCompleted
            );
            pc.addCancellationHistory(h);

            OrderProduct op = Optional.ofNullable(opById.get(item.getOrderProductId()))
                    .orElseThrow(() -> new CustomException(ErrorCode.ORDER_PRODUCT_NOT_BELONGS_TO_ORDER));

            op.cancelQuantityCheckChange(item.getCancelQuantity(), orderProductCanceled, orderProductPartiallyCanceled );
        }
        paymentsCancelRepository.save(pc);

        cancelOrder.changeStatusAfterItemCancellation(orderAllCanceled, orderPartialCanceled);
        return new CancelResponse("취소가 완료되었습니다.");
    }
}
