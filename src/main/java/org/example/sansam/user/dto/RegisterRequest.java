package org.example.sansam.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {
    private String email;
    private String password;
    private String mobileNumber;
    private Long salary;
    private boolean emailAgree;
}
