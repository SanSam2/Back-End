package org.example.sansam.cart.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.example.sansam.cart.domain.Cart;
import org.example.sansam.cart.dto.*;
import org.example.sansam.cart.repository.CartJpaRepository;
import org.example.sansam.product.domain.Product;
import org.example.sansam.product.domain.ProductDetail;
import org.example.sansam.product.dto.Option;
import org.example.sansam.product.repository.ProductDetailJpaRepository;
import org.example.sansam.product.service.ProductService;
import org.example.sansam.search.dto.SearchListResponse;
import org.example.sansam.user.domain.User;
import org.example.sansam.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

@Service
@AllArgsConstructor
public class CartService {
    private final CartJpaRepository cartJpaRepository;
    private final UserRepository userRepository;
    private final ProductService productService;
    private final ProductDetailJpaRepository productDetailJpaRepository;

    public void checkStock(Long detailId, Long quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("요청 수량은 1 이상이어야 합니다.");
        }
        ProductDetail productDetail = productDetailJpaRepository.findById(detailId).orElseThrow();
        if (productDetail.getQuantity() < quantity) {
            throw new IllegalArgumentException(
                    String.format("재고가 부족합니다. 현재 재고: %d, 요청 수량: %d", productDetail.getQuantity(), quantity)
            );
        }
    }

    @Transactional
    public void AddCartItem(AddCartRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없습니다."));
        List<AddCartItem> addCartItemList = request.getAddCartItems();
        if (addCartItemList.isEmpty()) {
            throw new IllegalArgumentException("추가할 상품이 없습니다.");
        }

        for (AddCartItem item : addCartItemList) {
            Long detailId = productService.getDetailId(item.getColor(), item.getSize(), item.getProductId());
            ProductDetail productDetail = productDetailJpaRepository.findById(detailId)
                    .orElseThrow(() -> new EntityNotFoundException("상품 옵션을 찾을 수 없습니다. detailId=" + detailId));
            Cart cart = cartJpaRepository.findByUserIdAndProductDetail(user, productDetail);
            if (cart != null) {
                Long changedQuantity = cart.getQuantity() + item.getQuantity();
                checkStock(detailId, changedQuantity);
                cart.setQuantity(changedQuantity);
                cartJpaRepository.save(cart);
                continue;
            }
            Cart newCart = new Cart();
            newCart.setUser(user);
            newCart.setQuantity(item.getQuantity());
            newCart.setProductDetail(productDetail);
            checkStock(detailId, item.getQuantity());
            cartJpaRepository.save(newCart);
        }
    }

    @Transactional
    public void deleteCartItem(DeleteCartRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없습니다."));
        List<DeleteCartItem> deleteCartItems = request.getDeleteCartItems();
        if (deleteCartItems.isEmpty()) {
            throw new IllegalArgumentException("삭제할 상품이 없습니다.");
        }
        for (DeleteCartItem item : deleteCartItems) {
            Long detailId = productService.getDetailId(item.getColor(), item.getSize(), item.getProductId());
            ProductDetail productDetail = productDetailJpaRepository.findById(detailId).orElseThrow();
            Cart cart = cartJpaRepository.findByUserIdAndProductDetail(user, productDetail);
            if( cart == null ) {
                throw new EntityNotFoundException("삭제할 상품 조회 실패" + item.getProductId());
            }
            cartJpaRepository.delete(cart);
        }
    }

    @Transactional(readOnly = true)
    public Page<SearchCartResponse> searchCarts(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없습니다."));
        Page<Cart> carts = cartJpaRepository.findAllByUser(user, pageable);
        return carts.map(cart -> {
            Option option = cart.getProductDetail().getOptionName();
            Product product = cart.getProductDetail().getProduct();
            SearchListResponse searchListResponse = SearchListResponse.builder()
                    .productId(product.getId())
                    .productName(product.getProductName())
                    .price(product.getPrice())
                    .url(product.getFileManagement().getMainFileDetail().getUrl())
                    .wish(false)
                    .build();

            return SearchCartResponse.builder()
                    .searchListResponse(searchListResponse)
                    .size(option.getSize())
                    .color(option.getColor())
                    .quantity(cart.getQuantity())
                    .stock(cart.getProductDetail().getQuantity())
                    .build();

        });
    }

    @Transactional
    public SearchCartResponse updateCart(UpdateCartRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없습니다."));
        Long detailId = productService.getDetailId(request.getColor(), request.getSize(), request.getProductId());
        ProductDetail productDetail = productDetailJpaRepository.findById(detailId)
                .orElseThrow(() -> new EntityNotFoundException("삭제할 상품이 없습니다."));

        Cart cart = cartJpaRepository.findByUserIdAndProductDetail(user, productDetail);
        if( cart == null ) {
            throw new EntityNotFoundException("장바구니에 해당 상품이 없습니다.");
        }
        checkStock(detailId, request.getQuantity());
        cart.setQuantity(request.getQuantity());
        cartJpaRepository.save(cart);

        return SearchCartResponse.builder()
                .searchListResponse(null)
                .size(productDetail.getOptionName().getSize())
                .color(productDetail.getOptionName().getColor())
                .quantity(cart.getQuantity())
                .stock(productDetail.getQuantity())
                .build();
    }
}

