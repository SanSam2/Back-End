package org.example.sansam.s3.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PresignedRequest {
    private String fileName;
    private String type;
    private Float size;
}
