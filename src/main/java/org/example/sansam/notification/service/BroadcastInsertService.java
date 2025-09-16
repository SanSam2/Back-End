package org.example.sansam.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.sansam.notification.domain.NotificationHistories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Log4j2
public class BroadcastInsertService {

    private final JdbcTemplate jdbcTemplate;
    private static final int BATCH_SIZE = 500;
    private static final int WORKER_COUNT = 4; // 병렬 worker 개수


    public void saveBroadcastNotification(List<NotificationHistories> histories) {
        if (histories.isEmpty()) return;

        long start = System.currentTimeMillis();
        log.info("️>>> BroadcastInsertService 시작 - total size={}, workerCount={}", histories.size(), WORKER_COUNT);

        // worker pool 생성
        ExecutorService executor = Executors.newFixedThreadPool(WORKER_COUNT);

        int chunkSize = (int) Math.ceil((double) histories.size() / WORKER_COUNT);

        for (int i = 0; i < histories.size(); i += chunkSize) {
            int end = Math.min(i + chunkSize, histories.size());
            List<NotificationHistories> partition = histories.subList(i, end);

            int workerId = i / chunkSize;
            executor.submit(() -> {
                long workerStart = System.currentTimeMillis();
                log.info(">>> Worker-{} 시작 (partition size={})", workerId, partition.size());
                try {
                    batchInsertHistories(workerId, partition);
                } catch (Exception e) {
                    log.error("❌ Worker-{} 실패", workerId, e);
                }
                log.info(">>> Worker-{} 완료 (elapsed={}ms)", workerId, System.currentTimeMillis() - workerStart);
            });

        }

        executor.shutdown();
        try {
            boolean finished = executor.awaitTermination(5, TimeUnit.MINUTES);
            if (!finished) {
                log.warn("⚠️ Broadcast Insert 작업이 제한 시간 내에 끝나지 않음");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Broadcast Insert 중단됨", e);
        }

        log.info("✅ Broadcast Insert 병렬 처리 완료 - 총 {}건", histories.size());
        log.info("✅ Broadcast Insert 전체 완료 - 총 {}건, elapsed={}ms", histories.size(), System.currentTimeMillis() - start);
    }

    private void batchInsertHistories(int workerId, List<NotificationHistories> histories) {
        String sql = "INSERT INTO notification_histories " +
                "(user_id, event_name, title, message, created_at, expired_at, is_read, notification_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        for (int i = 0; i < histories.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, histories.size());
            List<NotificationHistories> chunk = histories.subList(i, end);

            List<Object[]> batchParams = new ArrayList<>();
            for (NotificationHistories nh : chunk) {
                batchParams.add(new Object[]{
                        nh.getUser().getId(),
                        nh.getEventName(),
                        nh.getTitle(),
                        nh.getMessage(),
                        nh.getCreatedAt(),
                        nh.getExpiredAt(),
                        nh.isRead(),
                        nh.getNotification().getId()
                });
            }

            long batchStart = System.currentTimeMillis();
            jdbcTemplate.batchUpdate(sql, batchParams);
            log.debug("  Worker-{} ▶️ Batch Insert 완료 - chunk size={}, elapsed={}ms",
                    workerId, chunk.size(), System.currentTimeMillis() - batchStart);
        }
    }
}
