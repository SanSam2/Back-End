package org.example.sansam.global.event;

import java.time.Instant;
import java.util.UUID;

public record StockIncreaseRequestedEvent(
        String eventId,
        String aggregateId,
        Instant occurredAt,
        int SchemaVersion, //중복 처리 버전관리로, 버전이 늘어나게 되면 문제가 발생되도록 설정
        Long detailId,
        Integer quantity
) {
    public static StockIncreaseRequestedEvent of(String aggregateId,Long detailId, Integer quantity) {
        return new StockIncreaseRequestedEvent(
                UUID.randomUUID().toString(), //eventId
                aggregateId, //aggregateId
                Instant.now(),
                1,
                detailId,
                quantity
        );
    }

}