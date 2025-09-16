package org.example.sansam.stockreservation.waiter;


import org.example.sansam.stockreservation.domain.StockReservation;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class StockReservationWaiter {

    private static final long RECENT_TTL_MS = 1000;

    private final ConcurrentHashMap<String, CompletableFuture<StockReservation.Status>> waits
            = new ConcurrentHashMap<>();

    private final ConcurrentMap<String, Recent> recent = new ConcurrentHashMap<>();

    private record Recent(StockReservation.Status status, long ts) {
    }

    public CompletableFuture<StockReservation.Status> register(String orderId) {
        StockReservation.Status r = getRecent(orderId);
        if (r != null)
            return CompletableFuture.completedFuture(r);
        return waits.computeIfAbsent(orderId, k -> new CompletableFuture<>());
    }

    public StockReservation.Status getRecent(String orderId) {
        Recent r = recent.get(orderId);
        if (r == null)
            return null;
        if (System.currentTimeMillis() - r.ts > RECENT_TTL_MS) {
            recent.remove(orderId);
            return null;
        }
        return r.status;
    }

    public boolean complete(String orderId, StockReservation.Status s) {
        recent.put(orderId, new Recent(s, System.currentTimeMillis()));
        CompletableFuture<StockReservation.Status> remove = waits.remove(orderId);
        if (remove != null && !remove.isDone()){
            remove.complete(s);
            return true;
        }
        cleanupRecent(); //사이즈 너무 커지면 정리 -> 메모리 잡아먹을 테니까,,,
        return false;
    }

    public void discard(String orderId) {
        waits.remove(orderId);
    }

    private void cleanupRecent() {
        long now = System.currentTimeMillis();
        if (recent.size() > 10000) {
            for (Map.Entry<String, Recent> e : recent.entrySet()) {
                if (now - e.getValue().ts > RECENT_TTL_MS)
                    recent.remove(e.getKey(), e.getValue());
            }
        }
    }
}
