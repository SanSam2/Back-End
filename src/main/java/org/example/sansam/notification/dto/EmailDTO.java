package org.example.sansam.notification.dto;

import lombok.*;

@Builder
@Getter
public class EmailDTO {
    private  String to;
    private  String subject;
    private  String content;
}
