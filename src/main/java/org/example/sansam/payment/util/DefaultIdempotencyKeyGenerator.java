package org.example.sansam.payment.util;

import org.springframework.stereotype.Component;

@Component
public class DefaultIdempotencyKeyGenerator implements IdempotencyKeyGenerator {

    @Override
    public String forCancel(String paymentKey, long amount, String reason) {
        return IdempotencyKeyUtil.forCancel(paymentKey, amount, reason );
    }

}
