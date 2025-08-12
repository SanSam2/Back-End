package org.example.sansam.payment.service;

import lombok.RequiredArgsConstructor;

import org.example.sansam.exception.pay.CustomException;
import org.example.sansam.exception.pay.ErrorCode;
import org.example.sansam.notification.event.PaymentCancelEvent;
import org.example.sansam.notification.event.PaymentCanceledEmailEvent;
import org.example.sansam.order.domain.Order;
import org.example.sansam.order.domain.OrderProduct;
import org.example.sansam.order.repository.OrderRepository;
import org.example.sansam.payment.domain.PaymentCancellation;
import org.example.sansam.payment.domain.PaymentCancellationHistory;
import org.example.sansam.payment.dto.PaymentCancelRequest;
import org.example.sansam.payment.repository.PaymentsCancelRepository;
import org.example.sansam.status.domain.Status;
import org.example.sansam.status.domain.StatusEnum;
import org.example.sansam.status.repository.StatusRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PaymentCancelService {

    private final PaymentApiClient paymentApiClient;

    private final PaymentsCancelRepository paymentsCancelRepository;

    private final OrderRepository orderRepository;
    private final StatusRepository statusRepository;
    private final ApplicationEventPublisher eventPublisher;


    @Transactional
    public String wantToCancel(PaymentCancelRequest request) {
        try{
            //orderId 즉, orderNumber를 받아서, order찾기 (주문과 주문 상품 한방에 fetch join으로 가져옴)
            Order order = orderRepository.findOrderWithProducts(request.getOrderId());
            Status cancelRequested = statusRepository.findByStatusName(StatusEnum.ORDER_CANCEL_REQUESTED);
            Status orderPartialCanceled = statusRepository.findByStatusName(StatusEnum.ORDER_PARTIAL_CANCELED);
            Status orderAllCanceled = statusRepository.findByStatusName(StatusEnum.ORDER_ALL_CANCELED);
            Status cancelCompleted = statusRepository.findByStatusName(StatusEnum.CANCEL_COMPLETED);

            order.changeStatus(cancelRequested);
            Long cancelTotalAmount =order.getTotalAmount();
            int cancelTotalQuantity = 0;

            //orderProduct 상태값 변경
            List<PaymentCancellationHistory> histories = new ArrayList<>();
            for (OrderProduct op : order.getOrderProducts()) {
                op.updateOrderProductStatus(orderAllCanceled);

                PaymentCancellationHistory history = PaymentCancellationHistory.create(
                        op.getId(),         // orderProductId
                        op.getQuantity(),   // 전체수량만큼 취소
                        cancelRequested     // 취소요청받은 상태
                );
                cancelTotalQuantity +=op.getQuantity();
                histories.add(history);
            }


            //토스에서 진행
            String paymentKey = order.getPaymentKey();
            Map<String, Object> cancelResult = paymentApiClient.tossPaymentCancel(paymentKey,cancelTotalAmount, request.getCancelReason());

            // 취소 결과 검증
            if (cancelResult == null || !cancelResult.containsKey("status")) {
                throw new CustomException(ErrorCode.API_INTERNAL_ERROR);
            }

            //취소 응답을 통한 결과값 가져오기 (확인하기 일부 취소 잘 된건지)
            Object cancelsObj = cancelResult.get("cancels");
            if(!(cancelsObj instanceof List<?> cancelsList) || cancelsList.isEmpty()){
                throw new CustomException(ErrorCode.CANCEL_NOT_FOUND);
            }

            Object lastCancelObj = cancelsList.get(cancelsList.size()-1);
            if(!(lastCancelObj instanceof Map<?, ?> lastCancelMap)){
                throw new CustomException(ErrorCode.RESPONSE_FORM_NOT_RIGHT);
            }

            int cancelAmount = cancelTotalQuantity;
            String cancelReason = (String) lastCancelMap.get("cancelReason");
            String canceledAt = (String) lastCancelMap.get("canceledAt");
            LocalDateTime cancelDateTime = LocalDateTime.parse(canceledAt, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            Long refundPrice = lastCancelMap.get("cancelAmount")!= null ?
                    Long.parseLong(lastCancelMap.get("cancelAmount").toString()) : null;

            if(order.isAllCanceled()){
                order.changeStatus(orderAllCanceled);
            }else{
                order.changeStatus(orderPartialCanceled);
            }
            for (OrderProduct op : order.getOrderProducts()) {
                op.updateOrderProductStatus(orderAllCanceled);
            }

            // 취소 내역 저장
            PaymentCancellation cancellation = PaymentCancellation.create(paymentKey,
                    cancelAmount,
                    refundPrice,
                    cancelReason,
                    order.getId(),
                    cancelDateTime);
            for(PaymentCancellationHistory history : histories){
                history.changeStatus(cancelCompleted);
            }
            cancellation.addHistories(histories);
            paymentsCancelRepository.save(cancellation);
            PaymentCanceledEmailEvent event = new PaymentCanceledEmailEvent(order.getUser(), order.getOrderName(), cancelTotalAmount);
            eventPublisher.publishEvent(event);
            eventPublisher.publishEvent(new PaymentCancelEvent(order.getUser(), order.getOrderName(), cancelTotalAmount));

            // 주문 상태 CANCELED
            orderRepository.save(order);


            return "취소가 완료되었습니다.";
        }catch(Exception e){
            return "취소가 실패하였습니다. : "+e.getMessage();
        }
    }

}
