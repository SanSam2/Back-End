package org.example.sansam.notification.repository;

import org.example.sansam.notification.domain.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationsRepository extends JpaRepository<Notification, Long> {
    Optional<Notification> findByNotificationId(Long notificationId);
}
