package org.example.sansam.timedeal.controller;

import lombok.RequiredArgsConstructor;
import org.example.sansam.timedeal.dto.TimeDealDetailResponse;
import org.example.sansam.timedeal.dto.TimeDealResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/timedeals")
public class TimedealController {
    // 타임딜 리스트 조회, 상품 상세 조회
    // 타임딜 리스트 조회
    @GetMapping
    public ResponseEntity<?> getTimeDeals() {
        try {
            List<TimeDealResponse> response = new ArrayList<>();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    // 타임딜 상품 상세 조회
    @GetMapping("/detail")
    public ResponseEntity<?> getTimeDealDetail(
            @RequestParam Long productId,
            @RequestParam LocalDateTime startAt) {
        try {
            TimeDealDetailResponse response = new TimeDealDetailResponse();
            if (response == null) {
                return ResponseEntity.status(404).body("해당 타임딜 상품을 찾을 수 없습니다.");
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
            }
    }
}
