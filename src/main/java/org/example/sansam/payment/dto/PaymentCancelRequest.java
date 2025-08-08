package org.example.sansam.payment.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
public class PaymentCancelRequest {

    public String orderId;
    public String cancelReason;
    public List<CancelProductRequest> items;
}
