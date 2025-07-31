package org.example.sansam.s3.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "file_management")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileManagement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "type_name", nullable = false)
    private String typeName;

    @OneToMany(mappedBy = "fileManagement", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<FileDetail> fileDetails;

    public FileDetail getMainFileDetail() {
        if (fileDetails == null || fileDetails.isEmpty()) {
            return null;
        }

        return fileDetails.stream()
                .filter(detail -> Boolean.TRUE.equals(detail.getIsMain()))
                .findFirst()
                .orElse(fileDetails.get(0));
    }

    public FileDetail getFileDetail() {
        return fileDetails != null && !fileDetails.isEmpty() ? fileDetails.get(0) : null;
    }

    public String getFileUrl() {
        FileDetail detail = getFileDetail();
        return detail != null ? detail.getUrl() : null;
    }

}