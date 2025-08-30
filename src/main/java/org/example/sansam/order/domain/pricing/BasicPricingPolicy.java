package org.example.sansam.order.domain.pricing;

import org.example.sansam.order.domain.OrderProduct;

import java.util.List;

public final class BasicPricingPolicy implements PricingPolicy{

    @Override
    public Long totalOf(List<OrderProduct> products) {
        return products.stream().mapToLong(op -> {
            long unit = (op.getOrderedProductPrice() == null ? 0L : op.getOrderedProductPrice());
            int qty = op.getQuantity();
            int canceled = op.getCanceledQuantity();
            int effective = Math.max(0, qty - canceled);
            return unit * effective;
        }).sum();
    }
}