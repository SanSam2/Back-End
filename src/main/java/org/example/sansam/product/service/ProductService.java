package org.example.sansam.product.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.sansam.product.domain.*;
import org.example.sansam.product.dto.*;
import org.example.sansam.product.repository.ProductConnectJpaRepository;
import org.example.sansam.product.repository.ProductDetailJpaRepository;
import org.example.sansam.product.repository.ProductJpaRepository;
import org.example.sansam.s3.service.FileService;
import org.example.sansam.wish.repository.WishJpaRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.time.LocalDateTime;

import static org.example.sansam.product.domain.ProductStatus.AVAILABLE;
import static org.example.sansam.product.domain.ProductStatus.SOLDOUT;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductJpaRepository productJpaRepository;
    private final ProductDetailJpaRepository productDetailJpaRepository;
    private final ProductConnectJpaRepository productConnectJpaRepository;
    private final WishJpaRepository wishJpaRepository;
    private final FileService fileService;

    private static final int NEW_PRODUCT_PERIOD_DAYS = 14;

    //상품 상세 조회 -모든 옵션
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

    //default option 조회
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
                product.getStatus().name(),
                defaultDetail,
                isWish,
                reviewCount,
                new ArrayList<>(colors),
                new ArrayList<>(sizes)
        );
    }

    //option 선택
    public ProductDetailResponse getOptionByColor(Long productId, String color) {
        Product product = productJpaRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("상품이 없습니다."));

        Map<String, ProductDetailResponse> colorOptionMap = getProductOption(product, null, null);

        return Optional.ofNullable(colorOptionMap.get(color))
                .orElseThrow(() -> new EntityNotFoundException("해당 색상의 상품을 찾을 수 없습니다."));
    }

    //재고 조회 - 옵션 별
    public SearchStockResponse checkStock(SearchStockRequest request) {
        Product product = productJpaRepository.findById(request.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("상품이 없습니다."));
        Map<String, ProductDetailResponse> colorOptionMap = getProductOption(product, null, null);
        List<OptionResponse> optionResponses = colorOptionMap.get(request.getColor()).getOptions();
        if(optionResponses == null) {
            throw new EntityNotFoundException("해당 색상의 상품을 찾을 수 없습니다.");
        }
        Long quantity = 0L;
        for(OptionResponse option : optionResponses) {
            if(option.getSize().equals(request.getSize())) {
                quantity = option.getQuantity();
                break;
            }
        }

        return new SearchStockResponse(
                request.getProductId(),
                request.getSize(),
                request.getColor(),
                quantity
        );
    }

    //상품 상태 체크 - 상품 조회 전 실행, 품절처리 및 상태 변경
    public ProductStatusResponse checkProductStatus(Long productId) {
        Product product = productJpaRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("상품이 없습니다."));

        Map<String, ProductDetailResponse> colorOptionMap = getProductOption(product, null, null);
        boolean statusChanged = false;

        if (ProductStatus.NEW.equals(product.getStatus())) {
            LocalDateTime deadlineDate = product.getCreatedAt().plusDays(NEW_PRODUCT_PERIOD_DAYS);
            if (LocalDateTime.now().isAfter(deadlineDate)) {
                product.setStatus(ProductStatus.AVAILABLE);
                statusChanged = true;
            }
        }

        boolean isAllSoldOut = checkAllOptionsSoldOut(colorOptionMap);

        if (isAllSoldOut && !SOLDOUT.equals(product.getStatus())) {
            product.setStatus(SOLDOUT);
        } else if(!isAllSoldOut && SOLDOUT.equals(product.getStatus())) {
            product.setStatus(AVAILABLE);
            statusChanged = true;
        }

        if (statusChanged) {
            productJpaRepository.save(product);
        }

        return new ProductStatusResponse(
                product.getProductName(),
                product.getStatus().name()
        );
    }

    private boolean checkAllOptionsSoldOut(Map<String, ProductDetailResponse> colorOptionMap) {
        return colorOptionMap.values().stream()
                .allMatch(detail -> detail.getOptions().stream()
                        .allMatch(option -> option.getQuantity() <= 0));
    }

    private boolean matchProductDetail(ProductDetail detail, String targetColor, String targetSize) {
        boolean color = false;
        boolean size = false;
        for (ProductConnect connect : detail.getProductConnects()) {
            ProductOption option = connect.getOption();
            if (option.getType().equals("color") ) {
                color = option.getName().equals(targetColor);
            } else if (option.getType().equals("size")) {
                size = option.getName().equals(targetSize);
            }
        }
        return color && size;
    }

    //재고 변경
    @Transactional
    public SearchStockResponse changStock(ChangStockRequest request) throws Exception {
        Product product = productJpaRepository.findById(request.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("상품이 없습니다."));
        Map<String, ProductDetailResponse> colorOptionMap = getProductOption(product, null, null);
        ProductDetailResponse detailResponse = colorOptionMap.get(request.getColor());
        List<ProductDetail> productDetails = product.getProductDetails();
        ProductDetail findDetail = productDetails.stream()
                .filter(detail -> matchProductDetail(detail, request.getColor(), request.getSize()))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("상품이 없습니다."));
        if(findDetail == null) {
            throw new EntityNotFoundException("해당 옵션의 상품을 찾을 수 없습니다.");
        }

        Long stock = findDetail.getQuantity();
        if (request.getStatus().equals(ProductStatus.PLUS)) {
            findDetail.setQuantity(stock + request.getNum());
        } else if (request.getStatus().equals(ProductStatus.MINUS)) {
            if (stock < request.getNum()) {
                throw new Exception("재고가 부족합니다. 현재 재고: " + stock);
            }
            findDetail.setQuantity(stock - request.getNum());
        }

        productDetailJpaRepository.save(findDetail);
        return new SearchStockResponse(
                product.getId(),
                request.getSize(),
                request.getColor(),
                findDetail.getQuantity()
        );
    }
}