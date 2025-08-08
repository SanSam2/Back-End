package org.example.sansam.cart.service;

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

import java.util.List;

@Service
@AllArgsConstructor
public class CartService {
    private final CartJpaRepository cartJpaRepository;
    private final UserRepository userRepository;
    private final ProductService productService;
    private final ProductDetailJpaRepository productDetailJpaRepository;

    @Transactional
    public void AddCartItem(AddCartRequest request) {
        User user = userRepository.findById(request.getUserId()).orElseThrow();
        List<AddCartItem> addCartItemList = request.getAddCartItems();
        for(AddCartItem item : addCartItemList){
            Long detailId = productService.getDetailId(item.getColor(), item.getSize(), item.getProductId());
            ProductDetail productDetail = productDetailJpaRepository.findById(detailId).orElseThrow();
            Cart cart = new Cart();
            cart.setUser(user);
            cart.setQuantity(item.getQuantity());
            cart.setProductDetail(productDetail);
            cartJpaRepository.save(cart);
        }
    }

    @Transactional
    public void deleteCartItem(DeleteCartRequest request) {
        User user = userRepository.findById(request.getUserId()).orElseThrow();
        List<DeleteCartItem> deleteCartItems = request.getDeleteCartItems();
        for(DeleteCartItem item : deleteCartItems){
            Long detailId = productService.getDetailId(item.getColor(), item.getSize(), item.getProductId());
            ProductDetail productDetail = productDetailJpaRepository.findById(detailId).orElseThrow();
            Cart cart = cartJpaRepository.findByUserIdAndProductDetail(user, productDetail);
            cartJpaRepository.delete(cart);
        }
    }

    @Transactional(readOnly = true)
    public Page<SearchCartResponse> searchCarts(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        User user = userRepository.findById(userId).orElseThrow();
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
        User user = userRepository.findById(request.getUserId()).orElseThrow();
        Long detailId = productService.getDetailId(request.getColor(), request.getSize(), request.getProductId());
        ProductDetail productDetail = productDetailJpaRepository.findById(detailId).orElseThrow();

        Cart cart = cartJpaRepository.findByUserIdAndProductDetail(user, productDetail);
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
