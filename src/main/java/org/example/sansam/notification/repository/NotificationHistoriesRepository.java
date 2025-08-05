package org.example.sansam.notification.repository;

import org.example.sansam.notification.domain.NotificationHistories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;

@Repository
public interface NotificationHistoriesRepository extends JpaRepository<NotificationHistories, Long> {

    @Modifying
    @Transactional
    @Query("DELETE FROM NotificationHistories n WHERE n.expiredAt < :now")
    int deleteByExpiredAtBefore(Timestamp now);

    List<NotificationHistories> findAllByUser_Id(Long userId);

    void deleteByUser_IdAndNotification_Id(Long userId, Long notificationId);

    Long countByUser_IdAndIsReadFalse(Long userId);
    List<NotificationHistories> findAllByUser_IdAndIsReadFalse(Long userId);
}