package org.example.sansam.order.dto;

import org.example.sansam.order.domain.Order;

public class PaymentRequestEvent {

    private final Order order;

    public PaymentRequestEvent(Order order) {
        this.order = order;
    }

    public Order getOrder(){
        return order;
    }
}
