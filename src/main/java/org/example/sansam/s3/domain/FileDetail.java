package org.example.sansam.s3.domain;

import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "file_details")
@Builder
public class FileDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "file_details_id")
    private Long id;

    private String name;

    private String url;

    private Float size;

    private String extension;

    @Column(name = "is_main")
    private Boolean isMain;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_management_id", nullable = false)
    private FileManagement fileManagement;
}
