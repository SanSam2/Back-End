package org.example.sansam.order.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.sansam.exception.pay.CustomException;
import org.example.sansam.exception.pay.ErrorCode;
import org.example.sansam.order.domain.Order;
import org.example.sansam.order.domain.OrderProduct;
import org.example.sansam.order.dto.*;
import org.example.sansam.order.repository.OrderProductRepository;
import org.example.sansam.order.repository.OrderRepository;
import org.example.sansam.product.domain.Product;
import org.example.sansam.product.dto.ChangStockRequest;
import org.example.sansam.product.repository.ProductJpaRepository;
import org.example.sansam.product.service.ProductService;
import org.example.sansam.s3.service.FileService;
import org.example.sansam.status.domain.Status;
import org.example.sansam.status.domain.StatusEnum;
import org.example.sansam.status.repository.StatusRepository;
import org.example.sansam.user.domain.User;
import org.example.sansam.user.repository.UserRepository;

import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    //Order클래스 내부에 있는거니까 orderRepository에서 직접 꺼내고 수정
    private final OrderRepository orderRepository;
    private final OrderProductRepository orderProductRepository;
    private final ProductService productService;



    private final FileService fileService;
    private final UserRepository userRepository;
    private final StatusRepository statusRepository;
    private final ProductJpaRepository productJpaRepository;

    @Transactional
    public OrderResponse saveOrder(OrderRequest request){

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일을 가진 사용자를 찾을 수 없습니다."));


        String orderName = buildOrderName(request.getItems());
        String orderNumber = generateCustomOrderNumber();
        Status orderWaiting = statusRepository.findByStatusName(StatusEnum.ORDER_WAITING);
        Long totalAmount = calculateTotalAmount(request.getItems());
        Order order = Order.create(user,orderName,orderNumber,orderWaiting ,totalAmount,LocalDateTime.now());
        Order savedOrder = orderRepository.save(order);

        //재고 확인 후 재고 차감 로직 (2차때 꼭 다 갈아엎어버리기)
        for(OrderItemDto item : request.getItems()){

            //Product가 있는지 없는지확인을 하고
            Product product = productJpaRepository.findById(item.getProductId())
                    .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

            ChangStockRequest stockDecreaseRequest=new ChangStockRequest();

            stockDecreaseRequest.setProductId(product.getId());
            stockDecreaseRequest.setSize(item.getProductSize());
            stockDecreaseRequest.setColor(item.getProductColor());
            stockDecreaseRequest.setNum(item.getQuantity());

            try{
                productService.decreaseStock(stockDecreaseRequest); //재고차감 안에서 현재 재고에 대한 확인 진행
            }catch(Exception e) {
                throw new CustomException(ErrorCode.NO_ITEM);
            }

        }
        List<OrderItemResponseDto> responseDtos = saveOrderProducts(request.getItems(), savedOrder);
        return new OrderResponse(savedOrder, responseDtos);
    }

    //orderProduct 1차 주문 저장 -> Spring 스케줄러로 만약, 주문 완료 안되면 해당 주문 삭제
    private List<OrderItemResponseDto> saveOrderProducts(List<OrderItemDto> items, Order order) {
        Status orderProductWaiting = statusRepository.findByStatusName(StatusEnum.ORDER_PRODUCT_WAITING);
        List<OrderItemResponseDto> responseDtos=new ArrayList<>();

        for (OrderItemDto itemDto : items) {
            Product product = productJpaRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));
            Long fileManageUrl=product.getFileManagement().getId();
            String representativeUrl = fileService.getImageUrl(fileManageUrl);

            OrderProduct orderProduct = OrderProduct.create(order,product,itemDto.getProductPrice(),itemDto.getQuantity(),itemDto.getProductSize(),itemDto.getProductColor(),representativeUrl,orderProductWaiting);
            responseDtos.add(new OrderItemResponseDto(product.getId(),product.getProductName(),itemDto.getProductPrice(),itemDto.getProductSize(),itemDto.getProductColor(),itemDto.getQuantity(),representativeUrl));
            orderProductRepository.save(orderProduct);
        }
        return responseDtos;
    }

    //주문번호 생성 메서드 -> 이것도 도메인로직 안으로 집어넣어야한다.
    private String generateCustomOrderNumber() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    //주문명 생성 메소드 -> 도메인로직으로 들어가야함
    //만약 도메인로직으로 빠지면 dto를 참고하면 어떤 문제가 생길까요?
    private String buildOrderName(List<OrderItemDto> items) {
        int itemCount = items.size();
        if (itemCount == 0) throw new CustomException(ErrorCode.NO_ITEM_IN_ORDER);

        Long firstProductId = items.get(0).getProductId();
        Product firstProduct = productJpaRepository.findById(firstProductId)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

//        if (itemCount == 1) {
//            return firstProduct.getName();
//        } else {
//            return firstProduct.getName() + " 외 " + (itemCount - 1) + "건"; //메모리 관점에서 어떻게 돌아갈까요?
//        }

        return itemCount == 1 ? firstProduct.getProductName(): firstProduct.getProductName() + " 외 " + (itemCount - 1) + "건";
    }

    //총금액 계산 메소드
    private Long calculateTotalAmount(List<OrderItemDto> items) {
        Long totalAmount = 0L; //2^16  2^21? BigInteger
        for (OrderItemDto itemDto : items) {
            Product product = productJpaRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

            Long itemPrice = product.getPrice();
            totalAmount += itemPrice * itemDto.getQuantity();
        }
        return totalAmount;
    }

    @Transactional(readOnly = true)
    public Page<OrderWithProductsResponse> getAllOrdersByUserId(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        LocalDateTime from = LocalDateTime.now().minusDays(5);

        Page<Long> orderIdsPage = orderRepository.findRecentOrderIds(userId, from, pageable);
        if (orderIdsPage.isEmpty()) return Page.empty(pageable);

        List<Long> idOrder = orderIdsPage.getContent();

        List<Order> orders = orderRepository.findOrdersWithProductsFetchJoin(idOrder);

        // IN 순서 보장
        Map<Long, Integer> orderIndex = new HashMap<>();
        for (int i = 0; i < idOrder.size(); i++) orderIndex.put(idOrder.get(i), i);
        orders.sort(Comparator.comparingInt(o -> orderIndex.get(o.getId())));

        List<OrderWithProductsResponse> result = orders.stream().map(o -> {
            var items = o.getOrderProducts().stream()
                    .map(op -> new OrderWithProductsResponse.ProductSummary(
                            op.getId(),
                            op.getProduct().getId(),
                            op.getProduct().getProductName(),
                            op.getProduct().getPrice(),
                            op.getQuantity(),
                            op.getRepresentativeURL(),
                            op.getStatus().getStatusName()
                    ))
                    .toList();

            return new OrderWithProductsResponse(
                    o.getOrderNumber(),
                    o.getTotalAmount(),
                    o.getCreatedAt(),
                    o.getStatus().getStatusName(),
                    items
            );
        }).toList();

        return new PageImpl<>(result, pageable, orderIdsPage.getTotalElements());
    }
}
