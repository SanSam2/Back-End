package org.example.sansam.status.repository;

import org.example.sansam.status.Status;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StatusRepository extends JpaRepository<Status, Long> {
    Status findByStatusName(String statusName);
}
