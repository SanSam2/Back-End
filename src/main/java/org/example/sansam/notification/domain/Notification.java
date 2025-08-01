package org.example.sansam.notification.domain;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

@Entity
@Table(name = "notifications")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length=50, nullable=false)
    private String title;

    @Column(length=100, nullable=false)
    private String message;

    @Column(name = "created_at",nullable=false)
    private Timestamp createdAt;

}
