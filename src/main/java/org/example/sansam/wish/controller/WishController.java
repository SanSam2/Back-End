package org.example.sansam.wish.controller;

import lombok.RequiredArgsConstructor;
import org.example.sansam.wish.dto.AddWishRequest;
import org.example.sansam.wish.dto.DeleteWishRequest;
import org.example.sansam.wish.dto.SearchWishResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/wishes")
public class WishController {
    //위시 추가, 삭제, 리스트 조회,
    @PostMapping("/add")
    public ResponseEntity<?> addWish(@RequestBody AddWishRequest addWishRequest) {
        try{
            return ResponseEntity.ok("위시 추가 완료");
        }catch(IllegalArgumentException e){
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteWish(@RequestBody DeleteWishRequest deleteWishRequest) {
        try{
            return ResponseEntity.ok("위시 삭제 완료");
        }catch(IllegalArgumentException e){
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @PostMapping("/list")
    public ResponseEntity<?> searchWishList(@RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "10") Integer size) {
        try {
            List<SearchWishResponse> wishList = new ArrayList<>();
            return ResponseEntity.ok(wishList);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }
}
