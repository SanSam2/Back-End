package org.example.sansam.product.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.sansam.notification.event.sse.ProductQuantityLowEvent;
import org.example.sansam.product.domain.*;
import org.example.sansam.product.dto.*;
import org.example.sansam.product.repository.ProductDetailJpaRepository;
import org.example.sansam.product.repository.ProductJpaRepository;
import org.example.sansam.s3.domain.FileManagement;
import org.example.sansam.s3.service.FileService;
import org.example.sansam.search.scheduler.ProductSyncScheduler;
import org.example.sansam.status.domain.Status;
import org.example.sansam.status.domain.StatusEnum;
import org.example.sansam.status.repository.StatusRepository;
import org.example.sansam.wish.repository.WishJpaRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.*;
import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProductService {
    private final ProductJpaRepository productJpaRepository;
    private final ProductDetailJpaRepository productDetailJpaRepository;
    private final WishJpaRepository wishJpaRepository;
    private final FileService fileService;
    private final ApplicationEventPublisher publisher;
    private final StatusRepository statusRepository;
    private final ProductSyncScheduler scheduler;

    private final int MAX_TRY = 3;
    private final long BACKOFF_MS = 15;
    private static final int NEW_PRODUCT_PERIOD_DAYS = 14;


    private Map<String, ProductDetailResponse> getProductOption(Product product, Set<String> colors, Set<String> sizes) {
        Map<String, ProductDetailResponse> colorOptionMap = new LinkedHashMap<>();
        Map<String, String> colorImageMap = new HashMap<>();

        List<ProductDetail> details = productDetailJpaRepository.findByProductWithConnects(product);
        if (details == null || details.isEmpty()) {
            throw new EntityNotFoundException("상품 옵션 정보가 없습니다.");
        }

        for (ProductDetail detail : details) {
            List<ProductConnect> productConnects = detail.getProductConnects();
            if (productConnects == null || productConnects.isEmpty()) continue;

            String color = null;
            String size = null;

            for (ProductConnect connect : productConnects) {
                ProductOption option = connect.getOption();
                if (option == null || option.getType() == null) continue;

                if ("color".equals(option.getType())) {
                    color = option.getName();
                    if (colors != null) colors.add(color);
                } else if ("size".equals(option.getType())) {
                    size = option.getName();
                    if (sizes != null) sizes.add(size);
                }
            }

            if (color == null || size == null) continue;

            if (detail.getFileManagement() == null || detail.getFileManagement().getId() == null) {
                throw new EntityNotFoundException("상품 이미지 정보가 없습니다.");
            }
            colorImageMap.computeIfAbsent(color, c ->
                    fileService.getImageUrl(detail.getFileManagement().getId())
            );

            ProductDetailResponse productDetailResponse = colorOptionMap.computeIfAbsent(
                    color,
                    currentColor -> new ProductDetailResponse(currentColor, colorImageMap.get(currentColor), new ArrayList<>())
            );

            Long quantity = detail.getQuantity() != null ? detail.getQuantity() : 0L;
            productDetailResponse.getOptions().add(new OptionResponse(size, quantity));
        }

        return colorOptionMap;
    }


    //default option 조회
    @Transactional
    public ProductResponse getProduct(Long productId, Long userId) {
        Product product = productJpaRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("상품이 없습니다."));
        product.setViewCount(product.getViewCount() + 1);
        scheduler.updateProductData(productId);

        Set<String> colors = new LinkedHashSet<>();
        Set<String> sizes = new LinkedHashSet<>();
        Map<String, ProductDetailResponse> colorOptionMap = getProductOption(product, colors, sizes);

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
                product.getStatus() != null ? product.getStatus().getStatusName().name():StatusEnum.NEW.name(),
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
        ProductDetailResponse detail = colorOptionMap.get(canon(request.getColor()));
        if (detail == null) {
            throw new EntityNotFoundException("해당 색상의 상품을 찾을 수 없습니다.");
        }
        List<OptionResponse> optionResponses = detail.getOptions();
        Long quantity = 0L;
        for (OptionResponse option : optionResponses) {
            if(canon(option.getSize()).equals(canon(request.getSize()))) {
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

        StatusEnum current = product.getStatus() != null ? product.getStatus().getStatusName() : null;
        StatusEnum next = (current != null) ? current : StatusEnum.NEW;

        LocalDateTime deadlineDate = product.getCreatedAt().plusDays(NEW_PRODUCT_PERIOD_DAYS);
        if (next == StatusEnum.NEW && LocalDateTime.now().isAfter(deadlineDate)) {
            next = StatusEnum.AVAILABLE;
        }

        boolean allSoldOut = checkAllOptionsSoldOut(colorOptionMap);
        if (allSoldOut) {
            next = StatusEnum.SOLDOUT;
        } else if (next == StatusEnum.SOLDOUT) {
            next = StatusEnum.AVAILABLE;
        }

        if (next != current) {
            setStatus(product, next);
            productJpaRepository.save(product);
        }
        return new ProductStatusResponse(
                product.getProductName(),
                product.getStatus().getStatusName().name()
        );
    }

    private void setStatus(Product product, StatusEnum next) {
        Status status = statusRepository.findByStatusName(next);
        if (status == null) {
            throw new EntityNotFoundException("상태 엔티티가 없습니다: " + next);
        }
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
                color = canon(option.getName()).equals(canon(targetColor));
            } else if (option.getType().equals("size")) {
                size = canon(option.getName()).equals(canon(targetSize));
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
        if (detailResponse == null) {
            throw new EntityNotFoundException("해당 색상의 상품을 찾을 수 없습니다.");
        }
        List<ProductDetail> productDetails = product.getProductDetails();

        return productDetails.stream()
                .filter(detail -> matchProductDetail(detail, request.getColor(), request.getSize()))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("상품이 없습니다."));
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
                try {
                    Thread.sleep(BACKOFF_MS * attempt);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("재시도 대기 중 인터럽트됨", ie);
                }
                continue;
            }
            throw new OptimisticLockingFailureException(
                    increment ? "재고 증가 충돌" : "재고 감소 충돌"
            );
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
        List<ProductDetail> details = productDetailJpaRepository.findByProduct(productJpaRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("상품이 없습니다.")));
        for (ProductDetail detail : details) {
            if (matchProductDetail(detail, canon(color), canon(size))) {
                return detail.getId();
            }
        }
        throw new IllegalArgumentException("존재하지 않는 옵션입니다.");

    }

    private String canon(String s) {
        if (s == null) return null;
        return Normalizer.normalize(s.trim(), Normalizer.Form.NFKC)
                .toUpperCase(Locale.ROOT);
    }

//    public Product createProduct(String name) {
//        Product product = new Product();
//        product.setProductName(name);
//        product.setCreatedAt(LocalDateTime.now());
//        product.setViewCount(0L);
//        product.setStatus(statusRepository.findByStatusName(StatusEnum.NEW));
//        product = productJpaRepository.save(product);
//        return product;
//    }
//
//    @Transactional
//    public Product createProduct(String name, String brand, Long price, String fileUrl, String status) {
//        FileManagement file = fil
//        Product product = Product.builder()
//                .productName(name)
//                .brandName(brand)
//                .category(categoryJpaRepository.findById(1L)
//                        .orElseThrow(()-> new IllegalStateException("카테고리가 존재하지 않음")))
//                .price(price)
//                .status(statusRepository.findByStatusName(StatusEnum.NEW))
//                .createdAt(LocalDateTime.now())
//                .fileManagement(F)
//                .build();
//        return productJpaRepository.save(product);
//    }
}