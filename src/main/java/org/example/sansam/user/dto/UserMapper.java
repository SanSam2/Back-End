package org.example.sansam.user.dto;

import org.example.sansam.user.domain.User;

public class UserMapper {

    public static LoginResponse toLoginResponse(User user){
        return LoginResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .password(user.getPassword())
                .role(user.getRole().name())
                .build();
    }
}
