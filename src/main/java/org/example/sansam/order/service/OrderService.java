package org.example.sansam.order.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.sansam.order.domain.Order;
import org.example.sansam.order.domain.OrderProduct;
import org.example.sansam.order.dto.OrderItemDto;
import org.example.sansam.order.dto.OrderRequest;
import org.example.sansam.order.dto.OrderResponse;
import org.example.sansam.order.repository.OrderRepository;
import org.example.sansam.order.tmp.OrderStatus;
import org.example.sansam.order.tmp.Product;
import org.example.sansam.order.tmp.ProductRepository;
import org.example.sansam.user.domain.User;
import org.example.sansam.user.service.UserService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserService userService;
    private final ProductRepository productRepository;


    public OrderResponse saveOrder(OrderRequest request){
        Order order = new Order();

        String userEmail = request.getUserEmail();
        User user = userService.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일을 가진 사용자를 찾을 수 없습니다."));

        order.setOrderNumber(generateCustomOrderNumber());
        order.setUser(user);
        order.setCreatedAt(LocalDateTime.now());

        order.setStatus(OrderStatus.waiting); // 초기 상태 준비로 설정
        order.setOrderProducts(new ArrayList<>());

        int totalAmount = 0;

        log.error(order.getPaymentKey());
        for (OrderItemDto itemDto : request.getItems()) {
            Product product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다."));

            OrderProduct orderProduct = new OrderProduct();
            orderProduct.setOrder(order);
            orderProduct.setProduct(product);
            orderProduct.setQuantity((long) itemDto.getQuantity());


            int itemPrice = product.getPrice().intValue();
            totalAmount += itemPrice * itemDto.getQuantity();
            log.error(String.valueOf(totalAmount));

            order.getOrderProducts().add(orderProduct);
        }

        order.setTotalAmount(totalAmount);

        Order savedOrder = orderRepository.save(order);
        return new OrderResponse(savedOrder);
    }


    public Order getOrder(Long orderId){
        return orderRepository.findById(orderId)
                .orElseThrow(()-> new IllegalArgumentException("해당 주문이 조회되지 않습니다."));
    }

    public void markAsPaid(Long orderId) {
        Order order = getOrder(orderId);

        order.setStatus(OrderStatus.payComplete);
        orderRepository.save(order);
    }

    public void markAsFailed(Long orderId) {

        Order order = getOrder(orderId);

        order.setStatus(OrderStatus.payFailed);
        orderRepository.save(order);
    }

    public String generateCustomOrderNumber() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + "-" + UUID.randomUUID().toString().substring(0, 8);
    }




}
