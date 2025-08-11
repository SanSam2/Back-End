package org.example.sansam.chat.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.example.sansam.chat.dto.ChatRoomRequestDTO;
import org.example.sansam.chat.dto.ChatRoomResponseDTO;
import org.example.sansam.chat.dto.UserRoomResponseDTO;
import org.example.sansam.chat.service.ChatRoomService;
import org.example.sansam.user.dto.LoginResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/chatroom")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;


    // 입장한 채팅방 목록
    @Operation(summary = "입장한 채팅방 목록 조회", description = "로그인한 사용자가 입장한 채팅방 목록을 페이지 단위로 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "채팅방 목록 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserRoomResponseDTO.class),
                            examples = @ExampleObject(value = """
                {
                  "content": [
                    {
                      "id": 1,
                      "roomName": "개발자들의 모임",
                      "createdAt": "2025-08-11T13:30:00",
                      "lastMessageAt": "2025-08-11T14:00:00",
                      "messageCount": 123
                    }
                  ],
                  "pageable": {
                    "pageNumber": 0,
                    "pageSize": 20
                  },
                  "totalPages": 3,
                  "totalElements": 60,
                  "last": false,
                  "first": true,
                  "numberOfElements": 20
                }
            """)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "로그인 필요",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"로그인이 필요합니다.\"}"))
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"에러 메시지 내용\"}"))
            )
    })
    @GetMapping("/user")
    public ResponseEntity<?> UserChatRoomGet(HttpSession session,
                                             @RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "20") int size) {

        try {
            // 세션에서 loginUser 정보 가져오기
            LoginResponse loginUser = (LoginResponse) session.getAttribute("loginUser");

            // 로그인 정보 없으면 401
            if (loginUser == null) {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }

            Long userId = loginUser.getId();

            Page<UserRoomResponseDTO> rooms = chatRoomService.userRoomList(userId, page, size);
            return ResponseEntity.ok(rooms);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    // 채팅방 검색 및 전체조회
    @Operation(summary = "채팅방 검색 및 전체 조회", description = "키워드로 채팅방을 검색하거나 전체 채팅방 목록을 페이지 단위로 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "채팅방 목록 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ChatRoomResponseDTO.class),
                            examples = @ExampleObject(value = """
                {
                  "content": [
                    {
                      "id": 10,
                      "roomName": "스터디방",
                      "createdAt": "2025-08-11T12:00:00",
                      "lastMessageAt": "2025-08-11T14:00:00"
                    }
                  ],
                  "pageable": {
                    "pageNumber": 0,
                    "pageSize": 20
                  },
                  "totalPages": 2,
                  "totalElements": 40,
                  "last": false,
                  "first": true,
                  "numberOfElements": 20
                }
            """)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"에러 메시지 내용\"}"))
            )
    })
    @GetMapping
    public ResponseEntity<?> chatroom(@RequestParam(required = false) String keyword,
                                      @RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ChatRoomResponseDTO> chatRoomResponseDTO = chatRoomService.roomList(keyword, pageable);
        return new ResponseEntity<>(chatRoomResponseDTO, HttpStatus.OK);
    }

    // 채팅방 생성
    @Operation(summary = "채팅방 생성", description = "새로운 채팅방을 생성합니다. 로그인 필요.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "채팅방 생성 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ChatRoomResponseDTO.class),
                            examples = @ExampleObject(value = """
                {
                  "id": 101,
                  "roomName": "새로운 채팅방",
                  "createdAt": "2025-08-11T15:00:00",
                  "lastMessageAt": null
                }
            """)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "로그인 필요",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"로그인이 필요합니다.\"}")
                    )
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"에러 메시지 내용\"}")
                    )
            )
    })
    @PostMapping
    public ResponseEntity<?> createRoom(@RequestBody ChatRoomRequestDTO chatRoomRequestDTO, HttpSession session){

        try {
            LoginResponse loginUser = (LoginResponse) session.getAttribute("loginUser");

            if (loginUser == null) {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
            Long userId = loginUser.getId();

            ChatRoomResponseDTO chatRoomResponseDTO = chatRoomService.createRoom(chatRoomRequestDTO, userId);
            return  new ResponseEntity<>(chatRoomResponseDTO, HttpStatus.OK);
        } catch (Exception e){
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    // 채팅방입장
    @Operation(summary = "채팅방 입장", description = "로그인한 사용자가 특정 채팅방에 입장합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "채팅방 입장 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ChatRoomResponseDTO.class),
                            examples = @ExampleObject(value = """
                {
                  "id": 15,
                  "roomName": "팀 프로젝트 채팅방",
                  "createdAt": "2025-08-11T10:00:00",
                  "lastMessageAt": "2025-08-11T14:30:00"
                }
            """)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "로그인 필요",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"로그인이 필요합니다.\"}")
                    )
            ),
            @ApiResponse(responseCode = "500", description = "서버 에러",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"채팅방 입장 중 오류가 발생했습니다.\"}")
                    )
            )
    })
    @PostMapping("/{roomId}/enter")
    public ResponseEntity<?> chatroomEnter(@PathVariable Long roomId, HttpSession session) {

        try {
            LoginResponse loginUser = (LoginResponse) session.getAttribute("loginUser");

            if (loginUser == null) {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }

            Long userId = loginUser.getId();

            ChatRoomResponseDTO responseDTO = chatRoomService.enterRoom(roomId, userId);

            return ResponseEntity.ok(responseDTO);
        } catch (ResponseStatusException amount_ero) {
            throw amount_ero;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("채팅방 입장 중 오류가 발생했습니다.");
        }
    }


    // 채팅방 퇴장
    @Operation(summary = "채팅방 나가기", description = "로그인한 사용자가 특정 채팅방에서 나갑니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "채팅방 나가기 성공 (응답 바디 없음)"),
            @ApiResponse(responseCode = "401", description = "로그인 필요",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"로그인이 필요합니다.\"}")
                    )
            ),
            @ApiResponse(responseCode = "500", description = "서버 오류 발생",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"서버 내부 오류가 발생했습니다.\"}")
                    )
            )
    })
    @DeleteMapping("/{roomId}/leave")
    public ResponseEntity<?> chatroomLeave(@PathVariable Long roomId, HttpSession session) {

        try {
            LoginResponse loginUser = (LoginResponse) session.getAttribute("loginUser");

            if (loginUser == null) {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }

            Long userId = loginUser.getId();

            chatRoomService.roomLeave(roomId, userId);
            return ResponseEntity.noContent().build();
        }catch (Exception e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
