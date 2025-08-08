package org.example.sansam.status;


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
    private String statusName;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Status(String statusName) {
        this.statusName = statusName;
        this.createdAt = LocalDateTime.now();
    }
}
