package org.example.sansam.product.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {
//    private final ProductRepository productRepository;
//    private final ProductDetailJpaRepository productDetailJpaRepository;
//    private final ProductConnectJpaRepository productConnectJpaRepository;
//    private final WishJpaRepository wishJpaRepository;
//    private final FileService fileService;fileService
//
//    //상품 품절 처리, 상품 재고 조회, 상품 재고 차감
//    public SoldoutResponse markSoldOut(Long productId) {ß
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
//
//    public ProductResponse getDefaultOption(Long productId) {
//        Product product = productRepository.findById(productId)
//                .orElseThrow(() -> new EntityNotFoundException("상품이 없습니다."));
//
//        Map<String, OptionResponse> colorOptionMap = new LinkedHashMap<>();
//        Set<String> colors = new LinkedHashSet<>();
//        Set<String> sizes = new LinkedHashSet<>();
//        Map<String, String> colorImageMap = new HashMap<>();
//        List<ProductDetail> details = productDetailJpaRepository.findByProduct(product);
//        for (ProductDetail detail : details) {
//            String color = null, size = null;
//
//            for (ProductConnect connect : detail.getProductConnects()) {
//                ProductOption option = connect.getOption();
//                if ("color".equalsIgnoreCase(option.getType())) {
//                    color = option.getName();
//                    colors.add(color);
//                } else if ("size".equalsIgnoreCase(option.getType())) {
//                    size = option.getName();
//                    sizes.add(size);
//                }
//            }
//
//            if (color == null || size == null) continue;
//
//            String imageUrl = colorImageMap.computeIfAbsent(color,
//                    c -> fileService.getImageUrl(detail.getFileManagementId()));
//
//            OptionResponse sizeDto = new OptionResponse(size, detail.getQuantity());
//
//            colorOptionMap.computeIfAbsent(color, c -> new ProductDetailResponse(color, imageUrl, new ArrayList<>()))
//                    .getSizes().add(sizeDto);
//        }
//
//        String defaultColor = colors.stream().findFirst()
//                .orElseThrow(() -> new IllegalStateException("색상이 존재하지 않습니다."));
//        ProductDetailResponse productDetailResponse = colorOptionMap.get(defaultColor);
//        boolean isWish = wishJpaRepository.findByUserIdAndProductId(userId, productId).isPresent();
//
//        return new ProductResponse(
//                product.getId(),
//                product.getProductName(),
//                product.getCategory().toString(),
//                product.getBrandName(),
//                product.getPrice(),
//                product.getDescription(),
//                product.getFileManagement().getFileUrl(),
//                productDetailResponse,
//
//                new ArrayList<>(colors),
//                new ArrayList<>(sizes),
//        );
//    }
//
//    public ProductDetailResponse getOptionByColor(Long productId, String color) {
//        Product product = productRepository.findById(productId)
//                .orElseThrow(() -> new EntityNotFoundException("상품이 없습니다."));
//
//        String imageUrl = null;
//        List<OptionResponse> sizes = new ArrayList<>();
//        List<ProductDetail> details = productDetailJpaRepository.findByProduct(product);
//
//        for (ProductDetail detail : details) {
//            String currentColor = null;
//            String size = null;
//
//            for (ProductConnect connect : detail.getProductConnects()) {
//                ProductOption option = connect.getOption();
//                if ("color".equalsIgnoreCase(option.getType())) {
//                    currentColor = option.getName();
//                } else if ("size".equalsIgnoreCase(option.getType())) {
//                    size = option.getName();
//                }
//            }
//
//            if (!color.equals(currentColor) || size == null) continue;
//
//            if (imageUrl == null) {
//                imageUrl = fileService.getImageUrl(detail.getFileManagementId());
//            }
//
//            sizes.add(new OptionResponse(size, detail.getQuantity()));
//        }
//
//        return new ProductDetailResponse(color, imageUrl, sizes);
//    }
//
//
//    //색깔별로 전체 사이즈 재고 반환/ 색이랑 사이즈도 따로
}
