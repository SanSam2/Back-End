package org.example.sansam.user.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {

    private Long id;
    private String email;
    private String name;
    private String password;
    private String role;
}
