package org.example.sansam.product.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.sansam.product.domain.*;
import org.example.sansam.product.dto.*;
import org.example.sansam.product.repository.ProductConnectJpaRepository;
import org.example.sansam.product.repository.ProductDetailJpaRepository;
import org.example.sansam.product.repository.ProductJpaRepository;
import org.example.sansam.review.repository.ReviewJpaRepository;
import org.example.sansam.s3.service.FileService;
import org.example.sansam.wish.repository.WishJpaRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductJpaRepository productJpaRepository;
    private final ProductDetailJpaRepository productDetailJpaRepository;
    private final ProductConnectJpaRepository productConnectJpaRepository;
    private final WishJpaRepository wishJpaRepository;
    private final FileService fileService;

    private Map<String, ProductDetailResponse> getProductOption(Product product, Set<String> colors, Set<String> sizes) {
        Map<String, ProductDetailResponse> colorOptionMap = new LinkedHashMap<>();
        Map<String, String> colorImageMap = new HashMap<>();

        List<ProductDetail> details = productDetailJpaRepository.findByProduct(product);

        for (ProductDetail detail : details) {
            List<ProductConnect> productConnects = detail.getProductConnects();
            String color = null;
            String size = null;

            for (ProductConnect connect : productConnects) {
                ProductOption option = connect.getOption();
                if ("color".equals(option.getType())) {
                    color = option.getName();
                    if (colors != null) colors.add(color);
                } else if ("size".equals(option.getType())) {
                    size = option.getName();
                    if (sizes != null) sizes.add(size);
                }
            }

            if (color == null || size == null) continue;

            colorImageMap.computeIfAbsent(color, c ->
                    fileService.getImageUrl(detail.getFileManagementId())
            );

            ProductDetailResponse productDetailResponse = colorOptionMap.computeIfAbsent(
                    color,
                    currentColor -> new ProductDetailResponse(currentColor, colorImageMap.get(currentColor), new ArrayList<>())
            );

            productDetailResponse.getOptions().add(new OptionResponse(size, detail.getQuantity()));
        }

        return colorOptionMap;
    }

    public ProductResponse getProduct(Long productId, Long userId) {
        Product product = productJpaRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("상품이 없습니다."));

        Set<String> colors = new LinkedHashSet<>();
        Set<String> sizes = new LinkedHashSet<>();
        Map<String, ProductDetailResponse> colorOptionMap = getProductOption(product, colors, sizes);

        String defaultColor = colors.stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("색상이 존재하지 않습니다."));

        ProductDetailResponse defaultDetail = colorOptionMap.get(defaultColor);
        boolean isWish = wishJpaRepository
                .findByUserIdAndProductId(userId, productId).isPresent();
        Long reviewCount = productJpaRepository.countReviewsByProductId(productId);

        return new ProductResponse(
                product.getId(),
                product.getProductName(),
                product.getCategory().toString(),
                product.getBrandName(),
                product.getPrice(),
                product.getDescription(),
                product.getFileManagement() != null ? product.getFileManagement().getFileUrl() : null,
                defaultDetail,
                isWish,
                reviewCount,
                new ArrayList<>(colors),
                new ArrayList<>(sizes)
        );
    }

    public ProductDetailResponse getOptionByColor(Long productId, String color) {
        Product product = productJpaRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("상품이 없습니다."));

        Map<String, ProductDetailResponse> colorOptionMap = getProductOption(product, null, null);

        return Optional.ofNullable(colorOptionMap.get(color))
                .orElseThrow(() -> new EntityNotFoundException("해당 색상의 상품을 찾을 수 없습니다."));
    }
}
