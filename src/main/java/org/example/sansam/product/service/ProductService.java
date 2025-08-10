package org.example.sansam.product.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.sansam.notification.event.ProductQuantityLowEvent;
import org.example.sansam.product.domain.*;
import org.example.sansam.product.dto.*;
import org.example.sansam.product.repository.ProductConnectJpaRepository;
import org.example.sansam.product.repository.ProductDetailJpaRepository;
import org.example.sansam.product.repository.ProductJpaRepository;
import org.example.sansam.s3.service.FileService;
import org.example.sansam.status.domain.Status;
import org.example.sansam.status.domain.StatusEnum;
import org.example.sansam.status.repository.StatusRepository;
import org.example.sansam.wish.repository.WishJpaRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.*;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {
    private final ProductJpaRepository productJpaRepository;
    private final ProductDetailJpaRepository productDetailJpaRepository;
    private final WishJpaRepository wishJpaRepository;
    private final FileService fileService;
    private final ApplicationEventPublisher publisher;
    private final StatusRepository statusRepository;

    private final int MAX_TRY = 3;
    private final long BACKOFF_MS = 15;
    private static final int NEW_PRODUCT_PERIOD_DAYS = 14;

    private Map<String, ProductDetailResponse> getProductOption(Product product, Set<String> colors, Set<String> sizes) {
        Map<String, ProductDetailResponse> colorOptionMap = new LinkedHashMap<>();
        Map<String, String> colorImageMap = new HashMap<>();

        List<ProductDetail> details = productDetailJpaRepository.findByProductWithConnects(product);

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
                    fileService.getImageUrl(detail.getFileManagement().getId())
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
    @Transactional(readOnly = true)
    public ProductResponse getProduct(Long productId, Long userId) {
        Product product = productJpaRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("상품이 없습니다."));
        product.setViewCount(product.getViewCount() + 1);

        Set<String> colors = new LinkedHashSet<>();
        Set<String> sizes = new LinkedHashSet<>();
        Map<String, ProductDetailResponse> colorOptionMap =
                getProductOption(product, colors, sizes);

        String defaultColor = colors.stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("색상이 존재하지 않습니다."));

        ProductDetailResponse defaultDetail = colorOptionMap.get(defaultColor);
        boolean isWish = false;
        if (userId != null) {
            isWish = wishJpaRepository.findByUserIdAndProductId(userId, productId).isPresent();
        }
        Long reviewCount = productJpaRepository.countReviewsByProductId(productId);

        productJpaRepository.save(product);
        return new ProductResponse(
                product.getId(),
                product.getProductName(),
                product.getCategory().toString(),
                product.getBrandName(),
                product.getPrice(),
                product.getDescription(),
                product.getFileManagement() != null ? product.getFileManagement().getFileUrl() : null,
                product.getStatus().getStatusName().name(),
                defaultDetail,
                isWish,
                reviewCount,
                new ArrayList<>(colors),
                new ArrayList<>(sizes)
        );
    }

    //option 선택
    @Transactional(readOnly = true)
    public ProductDetailResponse getOptionByColor(Long productId, String color) {
        Product product = productJpaRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("상품이 없습니다."));

        Map<String, ProductDetailResponse> colorOptionMap = getProductOption(product, null, null);

        return Optional.ofNullable(colorOptionMap.get(canon(color)))
                .orElseThrow(() -> new EntityNotFoundException("해당 색상의 상품을 찾을 수 없습니다."));
    }

    //재고 조회 - 옵션 별
    @Transactional(readOnly = true)
    public SearchStockResponse checkStock(SearchStockRequest request) {
        Product product = productJpaRepository.findById(request.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("상품이 없습니다."));
        Map<String, ProductDetailResponse> colorOptionMap = getProductOption(product, null, null);
        List<OptionResponse> optionResponses = colorOptionMap.get(canon(request.getColor())).getOptions();
        if (optionResponses == null) {
            throw new EntityNotFoundException("해당 색상의 상품을 찾을 수 없습니다.");
        }
        Long quantity = 0L;
        for (OptionResponse option : optionResponses) {
            if (option.getSize().equals(canon(request.getSize()))) {
                quantity = option.getQuantity();
                break;
            }
        }

        return new SearchStockResponse(
                request.getProductId(),
                canon(request.getSize()),
                canon(request.getColor()),
                quantity
        );
    }

    //상품 상태 체크 - 상품 조회 전 실행, 품절처리 및 상태 변경
    @Transactional
    public ProductStatusResponse checkProductStatus(Long productId) {
        Product product = productJpaRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("상품이 없습니다."));
        Map<String, ProductDetailResponse> colorOptionMap = getProductOption(product, null, null);
        boolean statusChanged = false;

        Status status = product.getStatus();
        StatusEnum current = (status != null) ? status.getStatusName() : null;

        if (current == null) {
            setStatus(product, StatusEnum.NEW);
            statusChanged = true;
        }

        if (current == StatusEnum.NEW) {
            LocalDateTime deadlineDate = product.getCreatedAt().plusDays(NEW_PRODUCT_PERIOD_DAYS);
            if (LocalDateTime.now().isAfter(deadlineDate)) {
                setStatus(product, StatusEnum.AVAILABLE);
                statusChanged = true;
            }
        }

        boolean isAllSoldOut = checkAllOptionsSoldOut(colorOptionMap);
        if (isAllSoldOut && current != StatusEnum.SOLDOUT) {
            setStatus(product, StatusEnum.SOLDOUT);
            statusChanged = true;
        } else if (!isAllSoldOut && current == StatusEnum.SOLDOUT) {
            setStatus(product, StatusEnum.AVAILABLE);
            statusChanged = true;
        }

        if (statusChanged) {
            productJpaRepository.save(product);
        }

        return new ProductStatusResponse(
                product.getProductName(),
                product.getStatus().getStatusName().name()  // enum 이름 문자열로 반환
        );
    }

    private void setStatus(Product product, StatusEnum next) {
        Status status = statusRepository.findByStatusName(next);
        product.setStatus(status);
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
            if (option.getType().equals("color")) {
                color = option.getName().equals(canon(targetColor));
            } else if (option.getType().equals("size")) {
                size = option.getName().equals(canon(targetSize));
            }
        }
        return color && size;
    }

    //재고 조회
    public ProductDetail searchProductDetail(ChangStockRequest request) {
        Product product = productJpaRepository.findById(request.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("상품이 없습니다."));
        Map<String, ProductDetailResponse> colorOptionMap = getProductOption(product, null, null);
        ProductDetailResponse detailResponse = colorOptionMap.get(request.getColor());
        log.error(detailResponse.toString());
        List<ProductDetail> productDetails = product.getProductDetails();
        ProductDetail findDetail = productDetails.stream()
                .filter(detail -> matchProductDetail(detail, request.getColor(), request.getSize()))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("상품이 없습니다."));

        return findDetail;
    }

    private SearchStockResponse changeStock(ChangStockRequest request, boolean increment) {
        final Long productId = request.getProductId();
        final String size  = canon(request.getSize());   // 대소문자 정규화
        final String color = canon(request.getColor());
        final long num = request.getNum();

        for (int attempt = 1; attempt <= MAX_TRY; attempt++) {
            ProductDetail pd = productDetailJpaRepository
                    .findDetailByProductAndSizeColor(productId, "size", size, "color", color)
                    .orElseThrow(() -> new EntityNotFoundException("해당 옵션 조합이 없습니다."));

            long before = pd.getQuantity();

            if (!increment && before < num) {
                throw new IllegalArgumentException("재고가 부족합니다. 현재 재고: " + before);
            }
            int updated = increment
                    ? productDetailJpaRepository.tryIncrement(pd.getId(), num, pd.getVersion())
                    : productDetailJpaRepository.tryDecrement(pd.getId(), num, pd.getVersion());

            if (updated == 1) {
                long after = increment ? (before + num) : (before - num);
                if (!increment && before > 50 && after <= 50) {
                    publisher.publishEvent(new ProductQuantityLowEvent(pd));
                }

                return new SearchStockResponse(productId, size, color, after);
            }

            if (attempt < MAX_TRY) {
                try { Thread.sleep(BACKOFF_MS * attempt); } catch (InterruptedException ignored) {}
                continue;
            }
            throw new OptimisticLockingFailureException(increment ? "재고 증가 충돌" : "재고 변경 충돌");
        }
        throw new IllegalStateException("재고 처리 오류");
    }

    //재고 추가
    @Transactional
    public SearchStockResponse addStock(ChangStockRequest request) {
        return changeStock(request, /*increment=*/true);
    }

    //재고 감소
    @Transactional
    public SearchStockResponse decreaseStock(ChangStockRequest request) {
        return changeStock(request, /*increment=*/false);
    }

    public Long getDetailId(String color, String size, Long productId) {
        List<ProductDetail> details = productDetailJpaRepository.findByProduct(productJpaRepository.findById(productId).orElseThrow());
        for (ProductDetail detail : details) {
            if (matchProductDetail(detail, canon(color), canon(size))) {
                return detail.getId();
            }
        }
        return null;
    }

    private String canon(String s) {
        if (s == null) return null;
        return Normalizer.normalize(s.trim(), Normalizer.Form.NFKC)
                .toUpperCase(Locale.ROOT);
    }
}