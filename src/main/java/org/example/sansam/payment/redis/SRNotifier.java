package org.example.sansam.payment.redis;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.sansam.stockreservation.domain.StockReservation;
import org.example.sansam.stockreservation.waiter.StockReservationWaiter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SRNotifier {

    private final StockReservationWaiter waiter;

    public void handleMessage(String msg) {
        // 형식: orderId|CONFIRMED

        int p = msg.lastIndexOf('|');
        if (p <= 0) return;
        String orderId = msg.substring(0, p);
        String status  = msg.substring(p + 1);
        try {
            var s = StockReservation.Status.valueOf(status);
            // 각 노드의 로컬 대기자도 즉시 깨움
            boolean hit = waiter.complete(orderId, s);
            log.error("자자 이제 publish한다 Redis가 oid={} status={} notifyHit={}", orderId, s, hit);
        } catch (Exception ignore) {}
    }

}
