package org.example.sansam.notification.service;

import lombok.RequiredArgsConstructor;
import org.example.sansam.notification.domain.NotificationHistories;
import org.example.sansam.notification.domain.NotificationType;
import org.example.sansam.notification.exception.CustomException;
import org.example.sansam.notification.exception.ErrorCode;
import org.example.sansam.notification.repository.NotificationHistoriesRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationHistoryReader {
    private final NotificationHistoriesRepository repository;

    @Transactional(readOnly = true)
    public List<NotificationHistories> getMissedHistories(Long userId, String lastEventId) {
        Long lastId = Long.parseLong(lastEventId);
        return repository.findByUser_IdAndIdGreaterThan(userId, lastId);
    }

    @Transactional(readOnly = true)
    public NotificationHistories getLastBroadcastHistory() {
        return repository.findTopByEventNameOrderByIdDesc(NotificationType.BROADCAST.getEventName())
                .orElseThrow(() -> new CustomException(ErrorCode.NOTIFICATION_BROADCAST_NOT_FOUND));
    }
}
