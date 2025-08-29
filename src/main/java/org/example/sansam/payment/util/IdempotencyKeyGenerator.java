package org.example.sansam.payment.util;

public interface IdempotencyKeyGenerator {
    String forCancel(String paymentKey, long amount, String reason);
}
