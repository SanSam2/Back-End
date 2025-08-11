package org.example.sansam.wish.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.example.sansam.product.domain.Product;
import org.example.sansam.product.repository.ProductJpaRepository;
import org.example.sansam.s3.domain.FileManagement;
import org.example.sansam.user.domain.User;
import org.example.sansam.user.repository.UserRepository;
import org.example.sansam.wish.domain.Wish;
import org.example.sansam.wish.dto.AddWishRequest;
import org.example.sansam.wish.dto.DeleteWishItem;
import org.example.sansam.wish.dto.DeleteWishRequest;
import org.example.sansam.wish.dto.SearchWishResponse;
import org.example.sansam.wish.repository.WishJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Service
public class WishService {
    private WishJpaRepository wishJpaRepository;
    private UserRepository userRepository;
    private ProductJpaRepository productJpaRepository;

    @Transactional
    public void addWish(AddWishRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
        Product product = productJpaRepository.findById(request.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다."));
        boolean isExist = wishJpaRepository.findByUserIdAndProductId(user.getId(), product.getId()).isPresent();
        if (!isExist) {
            Wish wish = new Wish(user, product);
            wishJpaRepository.save(wish);
        }
    }

    @Transactional
    public void deleteWish(DeleteWishRequest request) {
        List<DeleteWishItem> deleteWishRequests = request.getDeleteWishItemList();

        List<Wish> wishes = new ArrayList<>();
        for(DeleteWishItem item : deleteWishRequests) {
            Wish wish = wishJpaRepository.findByUserIdAndProductId(item.getUserId(), item.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException("위시를 찾을 수 없습니다."));
            wishes.add(wish);
        }
        wishJpaRepository.deleteAll(wishes);
    }

    @Transactional(readOnly = true)
    public Page<SearchWishResponse> searchWishList(Long userId, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Wish> wishes = wishJpaRepository.findWishesByUserId(userId, pageable);

        return wishes.map(wish -> SearchWishResponse.builder()
                .productId(wish.getProduct().getId())
                .productName(wish.getProduct().getProductName())
                .url(wish.getProduct().getFileManagement().getMainFileDetail().getUrl())
                .build());
    }
}
