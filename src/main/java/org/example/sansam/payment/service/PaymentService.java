package org.example.sansam.payment.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.sansam.order.domain.Order;
import org.example.sansam.order.dto.OrderRequest;
import org.example.sansam.order.dto.OrderResponse;
import org.example.sansam.order.dto.PaymentRequestEvent;
import org.example.sansam.user.domain.User;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {
    private final ApplicationEventPublisher eventPublisher;


    public OrderResponse confirmPayment(OrderRequest orderRequest, User user){
        log.error ("confirmPayment 메서드 진입 완료");
        Order order = new Order();
        order.setUser(user);
        order.setCreatedAt(LocalDateTime.now());
        order.setPaymentKey(orderRequest.getPaymentKey());

        eventPublisher.publishEvent(new PaymentRequestEvent(order));
        return new OrderResponse(order);
    }


}
