package org.example.sansam.status.repository;

import org.example.sansam.status.domain.Status;
import org.example.sansam.status.domain.StatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StatusRepository extends JpaRepository<Status, Long> {
    Status findByStatusName(StatusEnum statusName);
}
