package org.example.sansam.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.sansam.exception.pay.CustomException;
import org.example.sansam.exception.pay.ErrorCode;
import org.example.sansam.order.domain.Order;
import org.example.sansam.order.repository.OrderRepository;
import org.example.sansam.payment.adapter.TossApprovalNormalizer;
import org.example.sansam.payment.adapter.TossApprovalNormalizer.Normalized;
import org.example.sansam.payment.compensation.service.PaymentCancelOutBoxService;
import org.example.sansam.payment.dto.TossPaymentRequest;
import org.example.sansam.payment.dto.TossPaymentResponse;
import org.example.sansam.payment.util.IdempotencyKeyGenerator;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final OrderRepository orderRepository;
    private final PaymentApiClient paymentApiClient;
    private final TossApprovalNormalizer normalizer;
    private final AfterConfirmTransactionService afterConfirmTransactionService;
    private final PaymentCancelOutBoxService outboxService;
    private final IdempotencyKeyGenerator idemGen;


    private static final String DB_FAILED_REASON = "db-failed";

    public TossPaymentResponse confirmPayment(TossPaymentRequest request){
        // 주문조회 -> 확실하게 결제 confirm해줘도 되는것인지에 대한 컨펌(?)

        //TODO: select * from order where ~~, select order_number from order where ~~ 랑 DB 측면에서 다르다!!!
        // Order를 다갖고오는게 맞을까????
        Order order = orderRepository.findByOrderNumber(request.getOrderId())
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        String orderNumber = order.getOrderNumber();

        //다시 진행하는 가격검증
        if(!Objects.equals(order.getTotalAmount(), request.getAmount())){
            throw new CustomException(ErrorCode.ORDER_AND_PAY_NOT_EQUAL);
        }

        //토스 payment로부터 온 응답 수령
        Map<String, Object> response;
        try {
            response = paymentApiClient.confirmPayment(request);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.API_FAILED, e);
        }

        //정규화 진행
        Normalized normalizePayment = normalizer.normalize(response, request.getPaymentKey());
        try {
            return afterConfirmTransactionService.approveInTransaction(orderNumber, normalizePayment);
        } catch (RuntimeException e) {
            String idempotencyKey = idemGen.forCancel(normalizePayment.paymentKey(), normalizePayment.totalAmount(), DB_FAILED_REASON);
            log.error("[CONFIRM] approveInTransaction failed. orderId={}, paymentKey={}",
                    order.getOrderNumber(), normalizePayment.paymentKey(), e);

            boolean canceled = bestEffortCancel(normalizePayment.paymentKey(),(Long) response.get("totalAmount"),DB_FAILED_REASON,idempotencyKey);
            if (!canceled) {
                outboxService.enqueue(normalizePayment.paymentKey(), (Long) response.get("totalAmount"), DB_FAILED_REASON,idempotencyKey);
            }

            throw new CustomException(ErrorCode.PAYMENT_FAILED);
        }
    }

    private boolean bestEffortCancel(String paymentKey,Long totalAmount, String reason, String idempotencyKey) {
        try {
            paymentApiClient.tossPaymentCancel(paymentKey, totalAmount, reason,idempotencyKey);
            return true;
        } catch (Exception ignore) {
            return false;
        }
    }
}
