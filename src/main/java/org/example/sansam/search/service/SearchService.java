package org.example.sansam.search.service;

import lombok.RequiredArgsConstructor;
import org.example.sansam.product.domain.Product;
import org.example.sansam.product.repository.ProductJpaRepository;
import org.example.sansam.s3.service.FileService;
import org.example.sansam.search.dto.SearchListResponse;
import org.example.sansam.wish.repository.WishJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Optional;

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

        return products.map(product -> {
            boolean isWished = userId != null &&
                    wishJpaRepository.findByUserIdAndProductId(userId, product.getId()).isPresent();

            String imageUrl = Optional.ofNullable(product.getFileManagement())
                    .map(file -> fileService.getImageUrl(file.getId()))
                    .orElse(null);

            return SearchListResponse.builder()
                    .productId(product.getId())
                    .productName(product.getProductName())
                    .price(product.getPrice().intValue())
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
}
