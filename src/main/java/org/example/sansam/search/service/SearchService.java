package org.example.sansam.search.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.sansam.product.domain.Product;
import org.example.sansam.product.dto.SearchStockResponse;
import org.example.sansam.product.repository.ProductJpaRepository;
import org.example.sansam.s3.service.FileService;
import org.example.sansam.search.dto.SearchListResponse;
import org.example.sansam.wish.domain.Wish;
import org.example.sansam.wish.repository.WishJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final ProductJpaRepository productJpaRepository;
    private final WishJpaRepository wishJpaRepository;
    private final FileService fileService;

    public Page<SearchListResponse> searchProductList(
            String keyword, String category, Long userId,
            int page, int size, String sort
    ) {
        Pageable pageable = PageRequest.of(page, size, getSort(sort));

        Page<Product> products = productJpaRepository.findByCategoryOrKeyword(keyword, category, pageable);
        Set<Long> wishedProductIds = new HashSet<>();
        if (userId != null) {
            List<Long> productsIds = products.getContent().stream()
                    .map(Product::getId)
                    .collect(Collectors.toList());
            wishedProductIds = wishJpaRepository.findByUserIdAndProductIdIn(userId, productsIds)
                    .stream()
                    .map(wish -> wish.getId())
                    .collect(Collectors.toSet());
        }
        final Set<Long> finalWishedProductIds = wishedProductIds;
        return products.map(product -> {
            boolean isWished = finalWishedProductIds.contains(product.getId());

            String imageUrl = Optional.ofNullable(product.getFileManagement())
                    .map(file -> fileService.getImageUrl(file.getId()))
                    .orElse(null);

            return SearchListResponse.builder()
                    .productId(product.getId())
                    .productName(product.getProductName())
                    .price(product.getPrice())
                    .url(imageUrl)
                    .wish(isWished)
                    .build();
        });
    }

    private Sort getSort(String sort) {
        return switch (sort) {
            case "wishCount" -> Sort.by(Sort.Direction.DESC, "wishCount");
            case "priceLow" -> Sort.by(Sort.Direction.ASC, "price");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
    }

    private List<SearchListResponse> productToDto(List<Product> products,Long userId) {
        return products.stream()
                .map(product -> {
                    boolean wished = (userId != null) &&
                            wishJpaRepository.findByUserIdAndProductId(userId, product.getId()).isPresent();
                    String imageUrl = (product.getFileManagement() != null)
                            ? fileService.getImageUrl(product.getFileManagement().getId())
                            : null;

                    return SearchListResponse.builder()
                            .productId(product.getId())
                            .productName(product.getProductName())
                            .price(product.getPrice()) // DTO가 int면 변환
                            .url(imageUrl)
                            .wish(wished)
                            .build();
                })
                .collect(Collectors.toList());
    }

    public List<SearchListResponse> getProductsByLike(Long userId) {
        List<Product> products = productJpaRepository.findTopWishListProduct();
        return productToDto(products, userId);
    }

    //상품 추천 - 위시에 상품이 있는 경우 -> 위시에 있는 상품과 같은 카테고리에 있는 상품 랜덤 추천 / 상품 위시에 없거나 유저 로그인X 시 -> 상품 조회순으로 표시
    public List<SearchListResponse> getProductsByRecommend(Long userId) {
        Wish wish = wishJpaRepository.findTopByUserIdOrderByCreated_atDesc(userId);
        List<Product> products;
        if(wish == null) {
            products = productJpaRepository.findTopWishListProduct();
        }else {
            Product product = productJpaRepository.findById(wish.getId())
                    .orElseThrow(() -> new EntityNotFoundException("상품이 없습니다."));
            products = productJpaRepository.findByCategoryOrderByViewCountDesc(product.getCategory());
        }
        return productToDto(products,userId);
    }
}
