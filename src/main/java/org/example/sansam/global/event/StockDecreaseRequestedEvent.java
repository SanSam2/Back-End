package org.example.sansam.global.event;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record StockDecreaseRequestedEvent(
        String eventId,
        String aggregateId,
        Instant occurredAt,
        int SchemaVersion, //중복 처리 버전관리로, 버전이 늘어나게 되면 문제가 발생되도록 설정
        List<orderInfoToStock> lines
) {
    public static StockDecreaseRequestedEvent of(String orderNumber, List<orderInfoToStock> lines) {
        return new StockDecreaseRequestedEvent(
                UUID.randomUUID().toString(), //eventId
                orderNumber, //aggregateId
                Instant.now(),
                1,
                lines
        );
    }

    public record orderInfoToStock(Long productId, Long detailId, int quantity){}
}
