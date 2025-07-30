package org.example.sansam.payment.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.sansam.order.domain.Order;
import org.example.sansam.order.dto.PaymentRequestEvent;
import org.example.sansam.order.repository.OrderRepository;
import org.example.sansam.order.tmp.OrderStatus;
import org.example.sansam.payment.dto.TossPaymentResponse;
import org.example.sansam.payment.infrastructure.TossPaymentClient;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentRequestEventListener {

    private final TossPaymentClient tossPaymentClient;

    @EventListener
    public void handlePaymentRequest(PaymentRequestEvent event){

        log.error("event 듣기 완료다라라라라");
        Order order = event.getOrder();

        String paymentKey = order.getPaymentKey();
        String orderId = String.valueOf(order.getId());
        int amount = order.getTotalAmount();


        try{
            TossPaymentResponse response = tossPaymentClient.requestPayment(paymentKey, orderId, amount);

            if("DONE".equals(response.getStatus())){
                order.setStatus(OrderStatus.payComplete);
                log.error(order.getStatus().toString());
            }else{
                order.setStatus(OrderStatus.payFailed);
            }
        }catch (Exception e){
            order.setStatus(OrderStatus.payFailed);
        }

    }
}
