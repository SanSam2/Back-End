package org.example.sansam.user.controller;


import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.example.sansam.user.dto.LoginRequest;
import org.example.sansam.user.dto.LoginResponse;
import org.example.sansam.user.dto.RegisterRequest;
import org.example.sansam.user.dto.UserMapper;
import org.example.sansam.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest requestDto){
        try{
            userService.register(requestDto);
            return ResponseEntity.ok("회원가입 완료");
        }catch(IllegalArgumentException e){
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpSession session) {
        return userService.login(request.getEmail(),request.getPassword())
                .map(user -> {
                    LoginResponse responseDto = UserMapper.toLoginResponse(user);
                    session.setAttribute("loginUser", responseDto);
                    return ResponseEntity.ok(responseDto);
                })
                .orElseGet(()-> ResponseEntity.status(401).build());
    }



    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok("로그아웃 완료");
    }

}
