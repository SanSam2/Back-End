package org.example.sansam.order.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.sansam.exception.pay.CustomException;
import org.example.sansam.exception.pay.ErrorCode;
import org.example.sansam.order.domain.Order;
import org.example.sansam.order.domain.OrderProduct;
import org.example.sansam.order.domain.nameformatter.KoreanOrdernameFormatter;
import org.example.sansam.order.domain.ordernumber.OrderNumberPolicy;
import org.example.sansam.order.domain.pricing.PricingPolicy;
import org.example.sansam.order.dto.*;
import org.example.sansam.order.mapper.OrderSummaryMapper;
import org.example.sansam.order.repository.OrderRepository;
import org.example.sansam.product.domain.Product;
import org.example.sansam.product.repository.ProductJpaRepository;
import org.example.sansam.product.service.ProductService;
import org.example.sansam.s3.service.FileService;
import org.example.sansam.status.domain.Status;
import org.example.sansam.status.domain.StatusEnum;
import org.example.sansam.status.repository.StatusRepository;
import org.example.sansam.stock.Service.StockService;
import org.example.sansam.user.domain.User;
import org.example.sansam.user.repository.UserRepository;

import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;



@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    //Order클래스 내부에 있는거니까 orderRepository에서 직접 꺼내고 수정
    private final OrderRepository orderRepository;
    private final ProductService productService;

    private final FileService fileService;
    private final UserRepository userRepository;
    private final StatusRepository statusRepository;
    private final ProductJpaRepository productJpaRepository;
    private final StockService stockService;

    private final OrderNumberPolicy orderNumberPolicy;
    private final PricingPolicy pricingPolicy;
    private final OrderSummaryMapper mapper;


    @Transactional
    public OrderResponse saveOrder(OrderRequest request){
        Status waiting = statusRepository.findByStatusName(StatusEnum.ORDER_WAITING);
        Status opWaiting = statusRepository.findByStatusName(StatusEnum.ORDER_PRODUCT_WAITING);

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.NO_USER_ERROR));

        //상품 정규화 -> 예를 들어서 그럴 일은 없으나, 상품 정보가 막 겹쳐서 들어오면? + 데드락 방지
        List<OrderItemDto> items = normalize(request.getItems());
        if (items.isEmpty()) throw new CustomException(ErrorCode.NO_ITEM_IN_ORDER);

        //상품 가격에서 한방에 로딩해서 불러와야되니까...
        Map<Long, Product> productMap = productJpaRepository.findAllById(
                items.stream().map(OrderItemDto::getProductId).toList()
        ).stream().collect(Collectors.toMap(Product::getId, p -> p));

        //가격 검증, 재고  차감
        for (OrderItemDto it : items) {
            Product p = Optional.ofNullable(productMap.get(it.getProductId()))
                    .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

            // 클라 가격 검증 (불일치 시 예외)
            if (!Objects.equals(p.getPrice(), it.getProductPrice())) {
                throw new CustomException(ErrorCode.PRICE_TAMPERING);
            }

            Long detailId = productService.getDetailId(
                    it.getProductColor(), it.getProductSize(), it.getProductId()
            );

            stockService.decreaseStock(detailId,it.getQuantity());
        }

        //order
        Order order = Order.create(user, waiting, orderNumberPolicy, LocalDateTime.now());

        //order에 필요한 OrderProduct 생성
        for (OrderItemDto it : items) {
            Product p = productMap.get(it.getProductId());
            String repUrl=fileService.getImageUrl(p.getFileManagement().getId());

            OrderProduct op = OrderProduct.create(
                    p, p.getPrice(), it.getQuantity(),
                    it.getProductSize(), it.getProductColor(), repUrl, opWaiting
            );
            order.addOrderProduct(op);
        }

        order.addOrderName(KoreanOrdernameFormatter.INSTANCE);
        order.calcTotal(pricingPolicy);

        orderRepository.save(order);
        List<OrderItemResponseDto> responseDtos = fromOrderToResponse(order);
        return new OrderResponse(order, responseDtos);
    }

    private List<OrderItemResponseDto> fromOrderToResponse(Order order){
        return order.getOrderProducts().stream().map(
                op-> new OrderItemResponseDto(
                        op.getProduct().getId(),
                        op.getProduct().getProductName(),
                        op.getOrderedProductPrice(),
                        op.getOrderedProductSize(),
                        op.getOrderedProductColor(),
                        op.getQuantity(),
                        op.getRepresentativeURL()
                )
        ).toList();
    }

    //같은 요청이 들어오는 경우 병합해야지????
    private List<OrderItemDto> normalize(List<OrderItemDto> items) {
        Map<String, OrderItemDto> merged = new LinkedHashMap<>();
        for (OrderItemDto it : items) {
            if (it.getQuantity() <= 0) {
                continue;
            }
            String key = it.getProductId() + "|" + it.getProductSize() + "|" + it.getProductColor();
            merged.merge(key, it, (a, b) ->
                    // id, name, price, size, color, quantity
                    new OrderItemDto(a.getProductId(),a.getProductName(),a.getProductPrice(),a.getProductSize(), a.getProductColor(),
                            a.getQuantity() + b.getQuantity()));
        }
        return new ArrayList<>(merged.values());
    }

    @Transactional(readOnly = true)
    public Page<OrderWithProductsResponse> getAllOrdersByUserId(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "id")));

        Page<Long> idPage = orderRepository.pageOrderIdsByUserId(userId, pageable);
        if (idPage.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }
        List<Order> loaded = orderRepository.findOrdersWithItemsByIds(idPage.getContent());
        Map<Long, Order> byId = loaded.stream()
                .collect(Collectors.toMap(Order::getId, o -> o));
        List<OrderWithProductsResponse> content = idPage.getContent().stream()
                .map(byId::get)
                .map(mapper::toOrderDto)
                .toList();
        return new PageImpl<>(content, pageable, idPage.getTotalElements());
    }
}
