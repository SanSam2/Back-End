package org.example.sansam.payment.dto;

import lombok.Getter;

@Getter
public class CancelResponse {
    String message;

    public CancelResponse(String message) {
        this.message = message;
    }
}
