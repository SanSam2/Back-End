package org.example.sansam.s3.repository;

import org.example.sansam.s3.domain.FileDetail;
import org.example.sansam.s3.domain.FileManagement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FileDetailJpaRepository extends JpaRepository<FileDetail, Long> {
    List<FileDetail> findByFileManagement(FileManagement fileManagement);
}
