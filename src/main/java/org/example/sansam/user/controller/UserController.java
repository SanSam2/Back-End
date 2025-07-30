package org.example.sansam.user.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    @Operation(summary = "회원가입", description = "사용자 정보를 받아 회원가입을 수행합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회원가입 성공"),
            @ApiResponse(responseCode = "400", description = "중복된 이메일 또는 유효하지 않은 입력")
    })
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest requestDto){
        try{
            userService.register(requestDto);
            return ResponseEntity.ok("회원가입 완료");
        }catch(IllegalArgumentException e){
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }


    @Operation(summary = "로그인", description = "이메일과 비밀번호를 입력 받아 세션을 생성하고 로그인합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패 (잘못된 이메일 또는 비밀번호)")
    })
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



    @Operation(summary = "로그아웃", description = "현재 로그인된 사용자의 세션을 종료합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그아웃 성공")
    })
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok("로그아웃 완료");
    }

}
