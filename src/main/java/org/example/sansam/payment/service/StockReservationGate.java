package org.example.sansam.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.sansam.stockreservation.cache.SRCache;
import org.example.sansam.stockreservation.domain.StockReservation;
import org.example.sansam.stockreservation.repository.StockReservationRepository;
import org.example.sansam.stockreservation.waiter.StockReservationWaiter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

@Service
@Slf4j
@RequiredArgsConstructor
public class StockReservationGate {

    private final @Qualifier("srTemplate") RedisTemplate<String, SRCache> cacheRedisTemplate;
    private final StockReservationRepository stockReservationRepository;
    private final StockReservationWaiter waiter;

    private StockReservation.Status readRedis(String orderId) {
        SRCache c = cacheRedisTemplate.opsForValue().get("sr:" + orderId);
        if (c == null)
            return null;
        if (!"PENDING".equals(c.status())) {
            return StockReservation.Status.valueOf(c.status());
        }
        return null;
    }

    public StockReservation.Status waitStatus(String orderId, long waitMillis) {
        StockReservation.Status recent = waiter.getRecent(orderId);
        if (recent != null) {
            waiter.discard(orderId);
            return recent;
        }

        StockReservation.Status cached = readRedis(orderId);
        if (cached != null)
            return cached;

        CompletableFuture<StockReservation.Status> future = waiter.register(orderId);
        StockReservation.Status recent2 = waiter.getRecent(orderId);
        if (recent2 != null) {
            waiter.discard(orderId);
            return recent2;
        }


        cached = readRedis(orderId);
        if (cached != null) {
            waiter.discard(orderId);
            return cached;
        }

        try{
            return future.orTimeout(waitMillis, MILLISECONDS).get();
        }catch(Exception e){
            cached = readRedis(orderId);
            return cached;
        }finally{
            waiter.discard(orderId);
        }
    }

}
