package org.example.sansam.product.service;

import com.amazonaws.services.ec2.model.ProductCodeValues;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.sansam.order.tmp.ProductRepository;
import org.example.sansam.product.domain.*;
import org.example.sansam.product.dto.*;
import org.example.sansam.product.repository.ProductConnectJpaRepository;
import org.example.sansam.product.repository.ProductDetailJpaRepository;
import org.example.sansam.product.repository.ProductJpaRepository;
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

    //상품 품절 처리, 상품 재고 조회, 상품 재고 차감
//    public SoldoutResponse markSoldOut(Long productId) {
//        Product product = productJpaRepository.findById(productId)
//                .orElseThrow(() -> new EntityNotFoundException("상품 정보를 찾을 수 없습니다."));
//
//        Status status = product.getStatus();
//        status.setStatusName("soldout");
//
//        SoldoutResponse response = SoldoutResponse.builder()
//                .productName(product.getProductName())
//                .size(produ)
//        return null;
//    }
//
//    public SearchStockResponse checkStock(SearchStockRequest request) {
//
//    }
//
//    public void decreaseStock(SearchStockRequest request) {
//
//    }

    public ProductResponse getProduct(Long productId, SearchRequest request) {
        Product product = productJpaRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("상품이 없습니다."));

        Map<String, ProductDetailResponse> colorOptionMap = new LinkedHashMap<>();
        Set<String> colors = new LinkedHashSet<>();
        Set<String> sizes = new LinkedHashSet<>();
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
                    colors.add(color);
                } else if ("size".equals(option.getType())) {
                    size = option.getName();
                    sizes.add(size);
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

        String defaultColor = colors.stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("색상이 존재하지 않습니다."));

        ProductDetailResponse defaultDetail = colorOptionMap.get(defaultColor);

        boolean isWish = wishJpaRepository
                .findByUserIdAndProductId(request.getUserId(), productId).isPresent();

        Long reviewCount = (long) product.getReviewList().size();

        return new ProductResponse(
                product.getId(),
                product.getProductName(),
                product.getCategory().toString(),
                product.getBrandName(),
                product.getPrice(),
                product.getDescription(),
                product.getFileManagement().getFileUrl(),
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

        String imageUrl = null;
        List<OptionResponse> sizes = new ArrayList<>();
        List<ProductDetail> details = productDetailJpaRepository.findByProduct(product);

        for (ProductDetail detail : details) {
            String currentColor = null;
            String size = null;

            for (ProductConnect connect : detail.getProductConnects()) {
                ProductOption option = connect.getOption();
                if ("color".equalsIgnoreCase(option.getType())) {
                    currentColor = option.getName();
                } else if ("size".equalsIgnoreCase(option.getType())) {
                    size = option.getName();
                }
            }

            if (!color.equals(currentColor) || size == null) continue;

            if (imageUrl == null) {
                imageUrl = fileService.getImageUrl(detail.getFileManagementId());
            }

            sizes.add(new OptionResponse(size, detail.getQuantity()));
        }

        return new ProductDetailResponse(color, imageUrl, sizes);
    }


    //색깔별로 전체 사이즈 재고 반환/ 색이랑 사이즈도 따로
}
