package org.example.sansam.payment.policy;

import org.example.sansam.order.domain.Order;
import org.example.sansam.payment.dto.CancelProductRequest;

import java.util.List;

public interface CancellationPolicy {
    void validate(Order order, List<CancelProductRequest> cancelProductRequests);
}
