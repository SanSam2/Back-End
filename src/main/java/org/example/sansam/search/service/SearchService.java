package org.example.sansam.search.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.sansam.product.domain.Product;
import org.example.sansam.product.dto.SearchStockResponse;
import org.example.sansam.product.repository.ProductJpaRepository;
import org.example.sansam.s3.service.FileService;
import org.example.sansam.search.dto.SearchItemResponse;
import org.example.sansam.search.dto.SearchListResponse;
import org.example.sansam.wish.domain.Wish;
import org.example.sansam.wish.repository.WishJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional(readOnly = true)
    public Page<SearchItemResponse> searchProductList(
            String keyword, String bigCategory, String middleCategory, String smallCategory,
            Long userId, int page, int size, String sort
    ) {
        Pageable pageable = PageRequest.of(page, size, getSort(sort));

        Page<Product> products = productJpaRepository.findByCategoryNamesOrKeyword(
                keyword, bigCategory, middleCategory, smallCategory, pageable);

        Set<Long> wishedProductIds = new HashSet<>();
        if (userId != null) {
            List<Long> productIds = products.getContent().stream()
                    .map(Product::getId)
                    .collect(Collectors.toList());

            wishedProductIds = wishJpaRepository.findByUserIdAndProductIdIn(userId, productIds)
                    .stream()
                    .map(wish -> wish.getProduct().getId())
                    .collect(Collectors.toSet());
        }

        final Set<Long> finalWishedProductIds = wishedProductIds;

        return products.map(product -> {
            boolean isWished = finalWishedProductIds.contains(product.getId());

            String imageUrl = Optional.ofNullable(product.getFileManagement())
                    .map(file -> fileService.getImageUrl(file.getId()))
                    .orElse(null);

            return SearchItemResponse.builder()
                    .productId(product.getId())
                    .productName(product.getProductName())
                    .price(product.getPrice())
                    .url(imageUrl)
                    .wish(isWished)
                    .category(product.getCategory().toString())
                    .build();
        });
    }

    private Sort getSort(String sortKey) {
        switch (sortKey) {
            case "price":
                return Sort.by(Sort.Direction.ASC, "price");
            case "viewCount":
                return Sort.by(Sort.Direction.DESC, "viewCount");
            case "createdAt":
            default:
                return Sort.by(Sort.Direction.DESC, "createdAt");
        }
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

    @Transactional(readOnly = true)
    public List<SearchListResponse> getProductsByLike(Long userId) {
        List<Product> products = productJpaRepository.findTopWishListProduct();
        return productToDto(products, userId);
    }


    //상품 추천 - 위시에 상품이 있는 경우 -> 위시에 있는 상품과 같은 카테고리에 있는 상품 랜덤 추천 / 상품 위시에 없거나 유저 로그인X 시 -> 상품 조회순으로 표시
    @Transactional(readOnly = true)
    public List<SearchListResponse> getProductsByRecommend(Long userId) {
        Wish wish = wishJpaRepository.findTopByUserIdOrderByCreated_atDesc(userId);
        List<Product> products;
        if(wish == null) {
            products = productJpaRepository.findProductsOrderByViewCountDesc();
        }else {
            Product product = productJpaRepository.findById(wish.getProduct().getId())
                    .orElseThrow(() -> new EntityNotFoundException("상품이 없습니다."));
            products = productJpaRepository.findByCategoryIdOrderByViewCountDesc(product.getCategory().getId());
        }
        return productToDto(products,userId);
    }
}
