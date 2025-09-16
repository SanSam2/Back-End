package org.example.sansam.stockreservation.cache;

public record SRCache(
        String status,          // "CONFIRMED" | "REJECTED"
        String requestEventId,  // 마지막 처리 기준 이벤트
        long updatedAtMs
) {}
