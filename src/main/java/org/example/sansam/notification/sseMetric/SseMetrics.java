package org.example.sansam.notification.sseMetric;

import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.example.sansam.notification.infra.SseConnector;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SseMetrics {

    private final SseConnector sseConnector;
    private final MeterRegistry meterRegistry;

    @PostConstruct
    public void init(){
        // gauge 등록 (active connections 수)
        meterRegistry.gauge("sse_connections_active", sseConnector.getAllEmitters(), map -> map.values().stream()
                .mapToInt(List::size)
                .sum());
    }
}
