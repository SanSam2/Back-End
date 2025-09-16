package org.example.sansam.notification.repository;

import org.example.sansam.notification.domain.NotificationHistories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationHistoriesRepository extends JpaRepository<NotificationHistories, Long> {

    @Modifying
    @Query("DELETE FROM NotificationHistories n WHERE n.expiredAt < :now")
    int deleteByExpiredAtBefore(@Param("now") LocalDateTime now);

    List<NotificationHistories> findAllByUser_Id(Long userId);

    @Modifying
    void deleteByUser_IdAndId(Long userId, Long id);

    Long countByUser_IdAndIsReadFalse(Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE NotificationHistories n
        SET n.isRead = true
        WHERE n.user.id = :userId AND n.isRead = false
    """)
    void findAllByUser_IdAndIsReadFalse(Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("""
        UPDATE NotificationHistories n
        SET n.isRead = true
        WHERE n.id = :notificationHistoriesId
    """)
    void findByIsReadFalse(Long notificationHistoriesId);
}