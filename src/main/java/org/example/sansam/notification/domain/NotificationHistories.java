package org.example.sansam.notification.domain;

import jakarta.persistence.*;
import lombok.*;
import org.example.sansam.user.domain.User;

import java.sql.Timestamp;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "notification_histories")
public class NotificationHistories {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_histories_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable=false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_id", nullable=false)
    private Notification notification;

    @Column(name = "created_at", nullable = false)
    private Timestamp createdAt;

    @Column(name = "expired_at", nullable = false)
    private Timestamp expiredAt;

    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String message;

}
