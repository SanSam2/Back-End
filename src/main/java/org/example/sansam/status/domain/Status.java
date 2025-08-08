package org.example.sansam.status.domain;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@Getter
public class Status {
    @Id
    @Column(name = "status_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long statusId;


    @Column(name = "status_name")
    @Enumerated(EnumType.STRING)
    private StatusEnum statusName;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Status(StatusEnum statusName) {
        this.statusName = statusName;
        this.createdAt = LocalDateTime.now();
    }
}
