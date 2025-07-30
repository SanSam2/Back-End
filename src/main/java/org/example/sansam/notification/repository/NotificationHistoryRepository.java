package org.example.sansam.notification.repository;

import jakarta.transaction.Transactional;
import org.example.sansam.notification.domain.NotificationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;

@Repository
public interface NotificationHistoryRepository extends JpaRepository<NotificationHistory, Long> {

    @Transactional
    @Modifying
//    @Query("DELETE FROM NotificationHistory nh Where nh.expired_at < :now")
    int deleteByExpiredAtBefore(@Param("now") Timestamp now);

}
