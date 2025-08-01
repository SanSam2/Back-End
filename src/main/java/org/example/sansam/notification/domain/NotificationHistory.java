package org.example.sansam.notification.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "notification_history")
public class NotificationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length=50, nullable=false, unique=true)
    private Long user_id;

    @Column(length=50, nullable=false)
    private Long notification_id;

    @Column(name = "created_at", nullable = false)
    private Timestamp createdAt;

    @Column(name = "expired_at", nullable = false)
    private Timestamp expiredAt;

    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    private String message;

}
