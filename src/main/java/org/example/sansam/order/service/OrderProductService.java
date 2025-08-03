package org.example.sansam.order.service;


import lombok.RequiredArgsConstructor;
import org.example.sansam.order.domain.OrderProduct;
import org.example.sansam.order.dto.OrderItemDto;
import org.example.sansam.order.repository.OrderProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderProductService {

    private final OrderProductRepository orderProductRepository;

    public List<OrderItemDto> findByOrderId(Long orderId) {
        List<OrderProduct> orderProducts = orderProductRepository.findByOrder_Id(orderId);


        return orderProducts.stream().map(orderProduct -> {
            OrderItemDto dto = new OrderItemDto();
            dto.setProductId(orderProduct.getProduct().getProductsId());
            dto.setProductPrice(orderProduct.getProduct().getPrice());
            dto.setQuantity(orderProduct.getQuantity().intValue());
            return dto;
        }).toList();
    }
}
