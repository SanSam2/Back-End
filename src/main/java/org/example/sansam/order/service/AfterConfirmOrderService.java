package org.example.sansam.order.service;

import lombok.RequiredArgsConstructor;
import org.example.sansam.global.event.StockDecreaseRequestedEvent;
import org.example.sansam.order.domain.Order;
import org.example.sansam.order.domain.OrderProduct;
import org.example.sansam.order.domain.nameformatter.KoreanOrdernameFormatter;
import org.example.sansam.order.domain.ordernumber.OrderNumberPolicy;
import org.example.sansam.order.domain.pricing.PricingPolicy;
import org.example.sansam.order.dto.OrderItemDto;
import org.example.sansam.order.dto.OrderResponse;
import org.example.sansam.order.mapper.OrderResponseMapper;
import org.example.sansam.order.publish.StockEventPublisher;
import org.example.sansam.order.repository.OrderRepository;
import org.example.sansam.product.domain.Product;
import org.example.sansam.product.service.ProductService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class AfterConfirmOrderService {

    private final ProductService productService;
    private final OrderRepository orderRepository;
    private final OrderNumberPolicy orderNumberPolicy;
    private final PricingPolicy pricingPolicy;
    private final OrderResponseMapper orderResponseMapper;

    private final StockEventPublisher stockEventPublisher;

    @Transactional
    public OrderResponse placeOrderTransaction(Preloaded pre,
                                                  List<OrderItemDto> items,
                                                  Map<Long, String> productImageUrl) {
        // 재고 차감시키기
        //TODO: 비동기 가능? -> 이벤트 객체 먼저 만들어놓고 ordernumber 생성되는대로 바로 보내버리자
        List<StockDecreaseRequestedEvent.orderInfoToStock> lines = items.stream()
                .map(it-> {
                    Long detailId =productService.getDetailId(
                            it.getProductColor(), it.getProductSize(), it.getProductId()
                    );

                    return new StockDecreaseRequestedEvent.orderInfoToStock(it.getProductId(), detailId, it.getQuantity());
                }).toList();
        // 주문/주문상품 생성
        Order order = Order.create(pre.user(), pre.waiting(), orderNumberPolicy, LocalDateTime.now());

        StockDecreaseRequestedEvent evt =
                StockDecreaseRequestedEvent.of(order.getOrderNumber(), lines);
        stockEventPublisher.publishStockDecreaseEvent(evt);

        for (OrderItemDto it : items) {
            Product p = pre.productMap().get(it.getProductId());
            String repUrl = productImageUrl.get(p.getId());
            OrderProduct op = OrderProduct.create(
                    p, p.getPrice(), it.getQuantity(),
                    it.getProductSize(), it.getProductColor(), repUrl, pre.opWaiting()
            );
            order.addOrderProduct(op);
        }//TODO: insert 쿼리 개수 확인

        order.addOrderName(KoreanOrdernameFormatter.INSTANCE);
        order.calcTotal(pricingPolicy);
        orderRepository.save(order);
        return orderResponseMapper.toDto(order);
    }

}
