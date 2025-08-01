package org.example.sansam.order.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.sansam.global.globaldto.OrderInfoDto;
import org.example.sansam.order.domain.Order;
import org.example.sansam.order.domain.OrderProduct;
import org.example.sansam.order.dto.OrderItemDto;
import org.example.sansam.order.dto.OrderRequest;
import org.example.sansam.order.dto.OrderResponse;
import org.example.sansam.order.repository.OrderRepository;
//import org.example.sansam.order.tmp.OrderStatus;
//import org.example.sansam.order.tmp.Product;
//import org.example.sansam.order.tmp.ProductService;
import org.example.sansam.user.domain.User;
import org.example.sansam.user.service.UserService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    //Order클래스 내부에 있는거니까 orderRepository에서 직접 꺼내고 수정
    private final OrderRepository orderRepository;

    //타 도메인에서 받아올때는 Service에서 받아오도록 설정
    private final UserService userService;
//    private final ProductService productService;


    public OrderResponse saveOrder(OrderRequest request){
        Order order = new Order();

        User user = userService.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일을 가진 사용자를 찾을 수 없습니다."));


        Long totalAmount = calculateTotalAmount(request.getItems());
        order.setTotalAmount(totalAmount);
        order.setOrderNumber(generateCustomOrderNumber());
        order.setUser(user);
        order.setCreatedAt(LocalDateTime.now());

        order.setStatus(OrderStatus.waiting); // 초기 상태 준비로 설정
        order.setOrderName(buildOrderName(request.getItems()));


        Order savedOrder = orderRepository.save(order);
        return new OrderResponse(savedOrder);
    }

    public Optional<Order> findById(Long orderId){
        Order order = orderRepository.findById(orderId).orElse(null);

        Order payOrder = new Order();
        payOrder.setId(order.getId());
        payOrder.setOrderNumber(order.getOrderNumber());
        payOrder.setOrderProducts(order.getOrderProducts());
        payOrder.setTotalAmount(order.getTotalAmount());
        payOrder.setStatus(OrderStatus.waiting);
        payOrder.setCreatedAt(LocalDateTime.now());

        return Optional.of(payOrder);
    }


    //주문번호 생성 메서드
    private String generateCustomOrderNumber() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    //주문명 생성 메소드
    private String buildOrderName(List<OrderItemDto> items) {
        int itemCount = items.size();
        if (itemCount == 0) throw new IllegalArgumentException("주문 상품이 비어있습니다.");

        Long firstProductId = items.get(0).getProductId();
        Product firstProduct = productService.findById(firstProductId)
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다."));

        if (itemCount == 1) {
            return firstProduct.getName();
        } else {
            return firstProduct.getName() + " 외 " + (itemCount - 1) + "건";
        }
    }

    //총금액 계산 메솓즈
    private Long calculateTotalAmount(List<OrderItemDto> items) {
        Long totalAmount = 0L;
        for (OrderItemDto itemDto : items) {
            Product product = productService.findById(itemDto.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다."));

            Long itemPrice = product.getPrice();
            totalAmount += itemPrice * itemDto.getQuantity();
        }
        return totalAmount;
    }

    private void fillOrderProducts(List<OrderItemDto> items, Order order) {
        for (OrderItemDto itemDto : items) {
            Product product = productService.findById(itemDto.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다."));

            OrderProduct orderProduct = new OrderProduct();
            orderProduct.setOrder(order);
            orderProduct.setProduct(product);
            orderProduct.setQuantity((long) itemDto.getQuantity());

            order.getOrderProducts().add(orderProduct);
        }
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



}
