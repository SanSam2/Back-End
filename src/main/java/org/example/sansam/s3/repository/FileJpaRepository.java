package org.example.sansam.s3.repository;

import org.example.sansam.s3.domain.FileManagement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileJpaRepository extends JpaRepository<FileManagement, Long> {
    
}
