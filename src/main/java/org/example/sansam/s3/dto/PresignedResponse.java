package org.example.sansam.s3.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PresignedResponse {
    private String url;
    private String accessUrl;
    private String fileName;
    private Float size;
}
