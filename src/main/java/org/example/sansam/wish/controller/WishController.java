package org.example.sansam.wish.controller;

import lombok.RequiredArgsConstructor;
import org.example.sansam.wish.dto.AddWishRequest;
import org.example.sansam.wish.dto.DeleteWishRequest;
import org.example.sansam.wish.dto.SearchWishResponse;
import org.example.sansam.product.dto.TextResponse;
import org.example.sansam.wish.service.WishService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/wishes")
public class WishController {
    private final WishService wishService;
    //위시 추가, 삭제, 리스트 조회,
    @PostMapping("/add")
    public ResponseEntity<?> addWish(@RequestBody AddWishRequest addWishRequest) {
        try{
            wishService.addWish(addWishRequest);
            return ResponseEntity.ok(new TextResponse("성공"));
        } catch(Exception e){
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteWish(@RequestBody DeleteWishRequest deleteWishRequest) {
        try{
            wishService.deleteWish(deleteWishRequest);
            return ResponseEntity.ok(new TextResponse("성공"));
        }catch(Exception e){
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @GetMapping("/list")
    public ResponseEntity<?> searchWishList(
            @RequestParam Long userId,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {
        try {
            Page<SearchWishResponse> wishList = wishService.searchWishList(userId, page, size);
            return ResponseEntity.ok(wishList);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }
}
