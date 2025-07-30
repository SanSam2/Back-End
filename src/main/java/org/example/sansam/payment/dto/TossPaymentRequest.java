package org.example.sansam.payment.dto;


import lombok.Builder;
import lombok.Getter;
import org.example.sansam.order.domain.Order;

@Getter
@Builder
public class TossPaymentRequest {

    private String orderId;
    private int amount;
    private String orderName;
    private String customerName;
    private String successUrl;
    private String failUrl;

    public static TossPaymentRequest form(Order order){

        int amount = order.getTotalAmount();

        return TossPaymentRequest.builder()
                .orderId(String.valueOf(order.getId()))
                .amount(amount)
                .orderName("Sansam 상품결제")
                .customerName(order.getUser().getEmail())
                .successUrl("")
                .failUrl("")
                .build();
    }
}
