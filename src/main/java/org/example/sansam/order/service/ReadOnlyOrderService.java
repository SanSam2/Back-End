package org.example.sansam.order.service;


import lombok.RequiredArgsConstructor;
import org.example.sansam.exception.pay.CustomException;
import org.example.sansam.exception.pay.ErrorCode;
import org.example.sansam.order.dto.OrderItemDto;
import org.example.sansam.product.domain.Product;
import org.example.sansam.product.repository.ProductJpaRepository;
import org.example.sansam.status.domain.Status;
import org.example.sansam.status.domain.StatusEnum;
import org.example.sansam.status.repository.StatusRepository;
import org.example.sansam.user.domain.User;
import org.example.sansam.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReadOnlyOrderService {

    private final UserRepository userRepository;
    private final ProductJpaRepository productJpaRepository;
    private final StatusRepository statusRepository;


    @Transactional(readOnly = true)
    public Preloaded preloadReadOnly(Long userId, List<OrderItemDto> items) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NO_USER_ERROR));

        Map<Long, Product> productMap = productJpaRepository.findAllById(
                items.stream().map(OrderItemDto::getProductId).toList()
        ).stream().collect(Collectors.toMap(Product::getId, p -> p));

        // 가격 검증. 상품 가격도 읽기만 하고 들어온 요청이랑 비교만 하니께
        for (OrderItemDto it : items) {
            Product p = Optional.ofNullable(productMap.get(it.getProductId()))
                    .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));
            if (!Objects.equals(p.getPrice(), it.getProductPrice())) {
                throw new CustomException(ErrorCode.PRICE_TAMPERING);
            }
        }

        Status waiting = statusRepository.findByStatusName(StatusEnum.ORDER_WAITING);
        Status opWaiting = statusRepository.findByStatusName(StatusEnum.ORDER_PRODUCT_WAITING);

        return new Preloaded(user, productMap, waiting, opWaiting);
    }
}
