package org.example.sansam.stockreservation.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.sansam.global.event.StockDecreaseResultEvent;
import org.example.sansam.kafka.topic.KafkaTopics;
import org.example.sansam.stockreservation.cache.SRCache;
import org.example.sansam.stockreservation.domain.StockReservation;
import org.example.sansam.stockreservation.repository.StockReservationRepository;
import org.example.sansam.stockreservation.waiter.StockReservationWaiter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Duration;

@Component
@RequiredArgsConstructor
@Slf4j
public class StockEventListener {

    private final StockReservationRepository stockReservationRepository;
    private final StockReservationWaiter waiter;
    private final @Qualifier("srTemplate") RedisTemplate<String, SRCache> cacheRedisTemplate;
    private final @Qualifier("stringRedisTemplate") RedisTemplate<String, String> stringRedisTemplate;

    private static final Duration TTL = Duration.ofSeconds(60);

    @KafkaListener(
            topics ={KafkaTopics.STOCK_DECREASE_CONFIRM, KafkaTopics.STOCK_DECREASE_REJECTED},
            containerFactory = "kafkaResultListenerContainerFactory"
    )
    @Transactional
    public void onResult(StockDecreaseResultEvent e) {
        StockReservation stockReservation = stockReservationRepository.findById(e.orderId())
                .orElseGet(() -> StockReservation.pending(e.orderId(), e.requestEventId()));

        if ("CONFIRMED".equals(e.type())) {
            stockReservation.markConfirmed(e.requestEventId());
        } else{
            stockReservation.markRejected(e.requestEventId(), e.reason());
        }

        stockReservationRepository.save(stockReservation);
        Runnable afterCommitTask = () -> {
            boolean hit = waiter.complete(e.orderId(), stockReservation.getStatus());

            try {
                String payload = e.orderId() + "|" + stockReservation.getStatus().name();
                stringRedisTemplate.convertAndSend("sr:ch", payload);
            } catch (Exception ex) {
                log.warn("Redis pub failed. oid={}", e.orderId(), ex);
            }

            try {
                SRCache cache = new SRCache(stockReservation.getStatus().name(), e.requestEventId(), System.currentTimeMillis());
                cacheRedisTemplate.opsForValue().set("sr:" + e.orderId(), cache, TTL);
            } catch (Exception ex) {
                log.error("Redis 세팅 실패. oid={}", e.orderId(), ex);
            }
            log.error("[RESULT] oid={} status={} notifyHit={}", e.orderId(), stockReservation.getStatus(), hit);
        };

        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override public void afterCommit() {
                    afterCommitTask.run();
                }
            });
        } else {
            // 트랜잭션 없으면 즉시 수행
            afterCommitTask.run();
        }

        log.info("[RESULT-SAVED] order = {} status={}",e.orderId(),stockReservation.getStatus());

    }
}
