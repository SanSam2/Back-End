package org.example.sansam.s3.repository;

import org.example.sansam.s3.domain.FileDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileDetailJpaRepository extends JpaRepository<FileDetail, Long> {

}
