package org.example.sansam.global.event;

import java.time.Instant;

public record StockDecreaseResultEvent(
        String type,
        String orderId,
        String requestEventId,
        String reason, //Reject당했을때의 사유
        Instant occuredAt
) {
    public static StockDecreaseResultEvent confirmed(String orderId, String requestEventId) {
        return new StockDecreaseResultEvent("CONFIRMED", orderId, requestEventId, null, Instant.now());
    }

    public static StockDecreaseResultEvent rejected(String orderId, String requestEventId,String reason) {
        return new StockDecreaseResultEvent("REJECTED", orderId, requestEventId, reason, Instant.now());
    }
}
