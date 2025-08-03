package org.example.sansam.s3.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.sansam.s3.domain.FileManagement;
import org.example.sansam.s3.domain.FileDetail;
import org.example.sansam.s3.repository.FileDetailJpaRepository;
import org.example.sansam.s3.repository.FileJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@Service
public class FileService {
    private final FileJpaRepository fileJpaRepository;
    private final FileDetailJpaRepository fileDetailJpaRepository;

    public FileManagement AddFile(String url, Float size) {
        String name = url.substring(url.lastIndexOf('/') + 1);
        String extension = name.contains(".") ? name.substring(name.lastIndexOf('.') + 1) : "jpg";

        FileManagement file = FileManagement.builder()
                .typeName("review")
                .build();
        Long fileId = fileJpaRepository.save(file).getId();

        FileDetail fileDetail = FileDetail.builder()
                .name(name)
                .url(url)
                .size(size)
                .extension(extension)
                .isMain(false)
                .fileManagement(fileJpaRepository.findById(fileId)
                        .orElseThrow(() -> new EntityNotFoundException("파일 정보를 찾을 수 없습니다.")))
                .build();

        Long id = fileDetailJpaRepository.save(fileDetail).getId();

        FileManagement saveFile = fileJpaRepository.findById(fileId)
                .orElseThrow(() -> new EntityNotFoundException("파일 정보를 찾을 수 없습니다."));

        return saveFile;
    }

    public String getImageUrl(Long fileManagementId) {
        FileManagement fileManagement = fileJpaRepository.findById(fileManagementId)
                .orElseThrow(() -> new EntityNotFoundException("파일 관리 정보를 찾을 수 없습니다."));

        List<FileDetail> fileDetails = fileDetailJpaRepository.findByFileManagement(fileManagement);
        if (fileDetails.isEmpty()) {
            throw new EntityNotFoundException("파일 상세 정보를 찾을 수 없습니다.");
        }

        // 메인 이미지가 있다면 그것을 반환하고, 없다면 첫 번째 이미지 반환
        return fileDetails.stream()
                .filter(FileDetail::getIsMain)
                .findFirst()
                .orElse(fileDetails.get(0))
                .getUrl();
    }

}
