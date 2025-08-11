package org.example.sansam.user.dto;

import org.example.sansam.user.domain.User;

public class UserMapper {

    public static LoginResponse toLoginResponse(User user){
        return LoginResponse.builder()
                .email(user.getEmail())
                .password(user.getPassword())
                .role(user.getRole().name())
                .build();
    }
}
