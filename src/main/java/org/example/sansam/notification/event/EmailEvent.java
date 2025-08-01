package org.example.sansam.notification.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class EmailEvent {

    private final String toEmail;
    private final String title;
    private final String body;

}
