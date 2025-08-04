package org.example.sansam.user.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<?> getMe(HttpSession session) {
        Object loginUser = session.getAttribute("loginUser");
        log.error(loginUser.toString());
        if (loginUser == null) {
            return ResponseEntity.status(401).body("로그인 필요");
        }
        return ResponseEntity.ok(loginUser);
    }

    @Operation(summary = "회원가입", description = "사용자 정보를 받아 회원가입을 수행합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회원가입 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "회원가입 완료")))
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

    @Operation(summary = "이메일 중복 체크", description = "입력한 이메일이 이미 존재하는지 확인합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "중복 여부 반환",
                    content = @Content(mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "중복 아님", value = "false"),
                                    @ExampleObject(name = "중복", value = "true")
                            }))
    })
    @PostMapping("/emailCheck")
    public Boolean emailCheck(@RequestBody String email){
        Boolean response = userService.ifSameEmail(email);
        return  response;
    }


    @Operation(summary = "로그인", description = "이메일과 비밀번호를 입력 받아 세션을 생성하고 로그인합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = LoginResponse.class),
                            examples = @ExampleObject(value = """
                            {
                                "id":1,
                                "email":"sansam@example.com",
                                "name":"강슬빈",
                                "password":"1234",
                                "role":"ADMIN"
                            }
                            """))),
            @ApiResponse(responseCode = "401", description = "인증 실패 (잘못된 이메일 또는 비밀번호)",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "Unauthorized"))),
            @ApiResponse(responseCode = "405", description = "잘못된 요청",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"timestamp\":\"2025-07-31T04:07:39.865+00:00\",\"status\":405,\"error\":\"Method Not Allowed\",\"path\":\"/api/users/login\"}")))
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
            @ApiResponse(responseCode = "200", description = "로그아웃 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "로그아웃 완료")))
    })
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok("로그아웃 완료");
    }

}
