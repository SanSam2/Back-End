package org.example.sansam.notification.Cleaner;

import lombok.RequiredArgsConstructor;
import org.example.sansam.notification.repository.NotificationHistoryRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class NotificationCleaner {
    private final NotificationHistoryRepository notificationHistoryRepository;

    @Scheduled(cron = "0 0 3 * * *") // 매일 새벽 3시에 만료 알림 삭제
    public void deleteExpiredNotification() {
        int deleted = notificationHistoryRepository.deleteByExpiredAtBefore(Timestamp.valueOf(LocalDateTime.now()));
        System.out.println("삭제된 만료 알림 수: " + deleted);
    }
}
