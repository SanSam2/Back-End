package org.example.sansam.order.domain.pricing;

import org.example.sansam.order.domain.OrderProduct;

import java.util.List;

public interface PricingPolicy {
    Long totalOf(List<OrderProduct> products);
}
