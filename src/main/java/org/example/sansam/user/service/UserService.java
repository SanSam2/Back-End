package org.example.sansam.user.service;

import lombok.RequiredArgsConstructor;
import org.example.sansam.user.domain.Role;
import org.example.sansam.user.domain.User;
import org.example.sansam.user.dto.RegisterRequest;
import org.example.sansam.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final String adminEmail = "sansam@example.com";

    public void register(RegisterRequest requestDto){

        Role role = requestDto.getEmail().equals(adminEmail) ? Role.ADMIN : Role.USER;

        User newUser = User.builder()
                .email(requestDto.getEmail())
                .password(requestDto.getPassword())
                .name(requestDto.getName())
                .mobileNumber(requestDto.getMobileNumber())
                .salary(requestDto.getSalary())
                .emailAgree(requestDto.isEmailAgree())
                .activated(true)
                .createdAt(LocalDateTime.now())
                .role(role)
                .build();

        userRepository.save(newUser);
    }

    public Optional<User> login(String email,String password) {

        return userRepository.findByEmail(email)
                .filter(user -> user.getPassword().equals(password));
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Boolean ifSameEmail (String email) {
        if(userRepository.findByEmail(email).isPresent()){
            return true;
        }else{
            return false;
        }
    }
}
