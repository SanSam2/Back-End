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
import org.example.sansam.order.mapper.OrderResponseMapper;
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


    private final StockService stockService;
    private final AfterConfirmOrderService afterConfirmOrderService;
    private final ReadOnlyOrderService readOnlyOrderService;

    private final OrderNumberPolicy orderNumberPolicy;
    private final PricingPolicy pricingPolicy;
    private final OrderSummaryMapper mapper;

    private int cnt=0;


    public OrderResponse saveOrder(OrderRequest request){
        //상품 정규화 -> 예를 들어서 그럴 일은 없으나, 상품 정보가 막 겹쳐서 들어오면?
        List<OrderItemDto> items = normalize(request.getItems());
        if (items.isEmpty())
            throw new CustomException(ErrorCode.NO_ITEM_IN_ORDER);

        LocalDateTime start1 = LocalDateTime.now();
        Preloaded pre = readOnlyOrderService.preloadReadOnly(request.getUserId(), items);
        LocalDateTime end1 = LocalDateTime.now();
        log.error(" preload time : {} ", end1.toInstant(java.time.ZoneOffset.UTC).toEpochMilli() - start1.toInstant(java.time.ZoneOffset.UTC).toEpochMilli());
        Map<Long, String> productImageUrl = new HashMap<>();

        LocalDateTime start2 = LocalDateTime.now();
        for (Product p : pre.productMap().values()) {
            productImageUrl.put(p.getId(), fileService.getImageUrl(p.getFileManagement().getId()));
        }
        LocalDateTime end2 = LocalDateTime.now();
        log.error(" getImageUrl time : {} ", end2.toInstant(java.time.ZoneOffset.UTC).toEpochMilli() - start2.toInstant(java.time.ZoneOffset.UTC).toEpochMilli());
        LocalDateTime start3 = LocalDateTime.now();
        OrderResponse orderResponse = afterConfirmOrderService.placeOrderTransaction(pre, items, productImageUrl);
        LocalDateTime end3 = LocalDateTime.now();
        log.error(" placeOrderTransaction time : {} ", end3.toInstant(java.time.ZoneOffset.UTC).toEpochMilli() - start3.toInstant(java.time.ZoneOffset.UTC).toEpochMilli());

        return orderResponse;
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
