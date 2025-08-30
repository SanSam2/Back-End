package org.example.sansam.order.service;


import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.example.sansam.exception.pay.CustomException;
import org.example.sansam.exception.pay.ErrorCode;
import org.example.sansam.order.domain.ordernumber.OrderNumberPolicy;
import org.example.sansam.order.dto.OrderItemDto;
import org.example.sansam.order.dto.OrderRequest;
import org.example.sansam.order.dto.OrderResponse;
import org.example.sansam.order.dto.OrderWithProductsResponse;
import org.example.sansam.order.repository.OrderRepository;
import org.example.sansam.product.domain.Category;
import org.example.sansam.product.domain.Product;
import org.example.sansam.product.service.ProductService;
import org.example.sansam.s3.domain.FileManagement;
import org.example.sansam.s3.service.FileService;
import org.example.sansam.status.domain.Status;
import org.example.sansam.status.domain.StatusEnum;
import org.example.sansam.stock.Service.StockService;
import org.example.sansam.user.domain.Role;
import org.example.sansam.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;


import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@Transactional
class OrderServiceTest {

    @Autowired
    private OrderService orderService;
    @Autowired
    private EntityManager em;

    @MockitoBean
    ProductService productService;
    @MockitoBean
    StockService stockService;
    @MockitoBean
    FileService fileService;

    private User user1;
    private User user2;
    private Status orderWaitingStatus;
    private Status orderProductWaiting;
    private Status orderPaid;
    private Product product1;
    private Product product2;
    private FileManagement fm1;
    private Category cat;
    private Status productStatus;
    @Autowired
    private OrderRepository orderRepository;

    @BeforeEach
    void init() {
        // ---- 공통 상태/유저/카테고리/상품 상태
        orderWaitingStatus = new Status(StatusEnum.ORDER_WAITING);
        em.persist(orderWaitingStatus);

        orderProductWaiting = new Status(StatusEnum.ORDER_PRODUCT_WAITING);
        em.persist(orderProductWaiting);

        orderPaid = new Status(StatusEnum.ORDER_PAID);
        em.persist(orderPaid);

        user1 = new User();
        user1.setEmail("xeulbn@test.com");
        user1.setName("xeulbn");
        user1.setPassword("1234");
        user1.setRole(Role.USER);
        user1.setEmailAgree(true);
        user1.setCreatedAt(LocalDateTime.now());
        em.persist(user1);

        user2 = new User();
        user2.setEmail("sansam@test.com");
        user2.setName("sansam");
        user2.setPassword("1234");
        user2.setRole(Role.USER);
        user2.setEmailAgree(true);
        user2.setCreatedAt(LocalDateTime.now());
        em.persist(user2);

        cat = new Category();
        cat.setBigName("TOPS");
        cat.setMiddleName("TEE");
        cat.setSmallName("BASIC");
        em.persist(cat);

        productStatus = new Status(StatusEnum.AVAILABLE);
        em.persist(productStatus);

        // ---- 파일/상품 (fileManagement 필드 채워 NPE 방지)
        fm1 = new FileManagement(); // 엔티티 제약에 맞춰야 하면 필드 더 채워도 됨
        em.persist(fm1);

        product1 = new Product();
        product1.setCategory(cat);
        product1.setStatus(productStatus);
        product1.setBrandName("NIKE");
        product1.setProductName("Air Tee 1");
        product1.setPrice(10000L);
        product1.setFileManagement(fm1);
        em.persist(product1);

        product2 = new Product();
        product2.setCategory(cat);
        product2.setStatus(productStatus);
        product2.setBrandName("NIKE");
        product2.setProductName("Air Tee 2");
        product2.setPrice(10000L);
        product2.setFileManagement(fm1);
        em.persist(product2);

        em.flush();

        // ---- 기본 stub: 재고 차감은 그냥 통과, 이미지 URL은 고정 값
        doNothing().when(stockService).decreaseStock(anyLong(), anyInt());
        doReturn("http://fake/url.jpg").when(fileService).getImageUrl(any());

        // detailId stub: 상품별로 다른 detailId 반환
        when(productService.getDetailId(eq("BLACK"), eq("M"), eq(product1.getId()))).thenReturn(101L);
        when(productService.getDetailId(eq("BLACK"), eq("M"), eq(product2.getId()))).thenReturn(202L);
        // 혹시 다른 인자 조합 들어오면 NPE 피하기 위한 기본값
        when(productService.getDetailId(anyString(), anyString(), anyLong())).thenReturn(999L);
        when(productService.getDetailId(eq("BLACK"), eq("M"), eq(product1.getId()))).thenReturn(101L);
        when(productService.getDetailId(eq("BLACK"), eq("M"), eq(product2.getId()))).thenReturn(202L);
    }

    //gpt
    private OrderRequest reqOf(Object... items) {
        OrderRequest r = new OrderRequest();
        r.setUserId(user1.getId());
        r.setItems(java.util.Arrays.stream(items).map(o -> (OrderItemDto) o).toList());
        return r;
    }

    private static class fakeOrderNumberPolicy implements OrderNumberPolicy {

        @Override
        public String makeOrderNumber() {
            return "1234567890-123e4567-e89b-12d3-a456-426614174000";
        }
    }

    @Test
    void 정상적으로_주문생성되고_정규화하고_재고차감_후_저장() {
        // given: p1(2), p1(1) -> 정규화로 p1=3, p2(1)
        OrderRequest orderRequest = reqOf(
                new OrderItemDto(product1.getId(), product1.getProductName(), product1.getPrice(), "M", "BLACK", 2),
                new OrderItemDto(product1.getId(), product1.getProductName(), product1.getPrice(), "M", "BLACK", 1),
                new OrderItemDto(product2.getId(), product2.getProductName(), product2.getPrice(), "M", "BLACK", 1)
        );

        // when
        OrderResponse response = orderService.saveOrder(orderRequest);

        // then
        assertThat(response.getOrderNumber()).isNotNull();
        assertThat(response.getItems()).hasSize(2);

        var itemP1 = response.getItems().stream()
                .filter(i -> i.getProductId().equals(product1.getId()))
                .findFirst().orElseThrow();
        assertThat(itemP1.getQuantity()).isEqualTo(3);
        assertThat(itemP1.getProductName()).isEqualTo(product1.getProductName());
        assertThat(itemP1.getProductPrice()).isEqualTo(product1.getPrice());
        assertThat(itemP1.getOrderProductImageUrl()).isEqualTo("http://fake/url.jpg");

        var itemP2 = response.getItems().stream()
                .filter(i -> i.getProductId().equals(product2.getId()))
                .findFirst().orElseThrow();
        assertThat(itemP2.getQuantity()).isEqualTo(1);
        assertThat(itemP2.getProductPrice()).isEqualTo(product2.getPrice());
        assertThat(itemP2.getOrderProductImageUrl()).isEqualTo("http://fake/url.jpg");

        // 총액 = (3 + 1) * 10000
        assertThat(response.getTotalAmount()).isEqualTo(40000L);
    }

    @Test
    void 없는_유저면_NO_USER_ERROR() {
        //given
        OrderRequest req = new OrderRequest();
        req.setUserId(999_999L);
        req.setItems(List.of(
                new OrderItemDto(product1.getId(), "XoXo", 0L, "M", "BLACK", 1)
        ));
        //when & then
        CustomException ex = assertThrows(CustomException.class, () -> orderService.saveOrder(req));
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.NO_USER_ERROR);
    }

    @Test
    void 없는_상품이면_PRODUCT_NOT_FOUND() {
        //givne
        OrderRequest req = reqOf(
                new OrderItemDto(999_999L, "XoXo", 0L, "M", "BLACK", 1)
        );

        //when & then
        CustomException ex = assertThrows(CustomException.class, () -> orderService.saveOrder(req));
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.PRODUCT_NOT_FOUND);
    }

    @Test
    void 빈_장바구니면_NO_ITEM_IN_ORDER() {
        //given
        OrderRequest req = new OrderRequest();
        req.setUserId(user1.getId());
        req.setItems(List.of());

        //when & then
        CustomException ex = assertThrows(CustomException.class, () -> orderService.saveOrder(req));
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.NO_ITEM_IN_ORDER);
    }

    @Test
    void 가격_위변조면_PRICE_TAMPERING() {
        //given
        OrderRequest req = reqOf(
                new OrderItemDto(product1.getId(), product1.getProductName(), product1.getPrice() + 1, "M", "BLACK", 1)
        );

        //when & then
        CustomException ex = assertThrows(CustomException.class, () -> orderService.saveOrder(req));
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.PRICE_TAMPERING);
    }

    @Test
    void 정규화_후_재고차감_호출수와_파라미터_검증() {
        // given
        OrderRequest orderRequest = reqOf(
                new OrderItemDto(product1.getId(), product1.getProductName(), product1.getPrice(), "M", "BLACK", 2),
                new OrderItemDto(product1.getId(), product1.getProductName(), product1.getPrice(), "M", "BLACK", 1),
                new OrderItemDto(product2.getId(), product2.getProductName(), product2.getPrice(), "M", "BLACK", 1)
        );

        // when
        orderService.saveOrder(orderRequest);

        // then
        verify(stockService, times(1)).decreaseStock(eq(101L), eq(3));
        verify(stockService, times(1)).decreaseStock(eq(202L), eq(1));
        verify(stockService, times(2)).decreaseStock(anyLong(), anyInt());
    }

    @Test
    void 대표이미지_URL_조회는_아이템_수만큼_호출된다() {
        // given
        OrderRequest orderRequest = reqOf(
                new OrderItemDto(product1.getId(), product1.getProductName(), product1.getPrice(), "M", "BLACK", 1),
                new OrderItemDto(product2.getId(), product2.getProductName(), product2.getPrice(), "M", "BLACK", 1)
        );

        // when
        orderService.saveOrder(orderRequest);

        // then
        verify(fileService, times(2)).getImageUrl(eq(fm1.getId()));
    }

    @Test
    void 수량0은_정규화에서_제외되어_주문에_포함되지_않는다() {
        // given
        OrderRequest orderRequest = reqOf(
                new OrderItemDto(product1.getId(), product1.getProductName(), product1.getPrice(), "M", "BLACK", 0),
                new OrderItemDto(product2.getId(), product2.getProductName(), product2.getPrice(), "M", "BLACK", 2)
        );

        // when
        OrderResponse response = orderService.saveOrder(orderRequest);

        // then
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getProductId()).isEqualTo(product2.getId());
        assertThat(response.getItems().get(0).getQuantity()).isEqualTo(2);
        verify(stockService, never()).decreaseStock(eq(101L), anyInt()); // p1은 호출 안 됨
        verify(stockService, times(1)).decreaseStock(eq(202L), eq(2));   // p2만
    }


    @Test
    void 사용자별_주문내역_페이징_조회_내용검증() {
        // given
        var r1 = reqOf(new OrderItemDto(product1.getId(), product1.getProductName(), product1.getPrice(), "M", "BLACK", 1));
        var r2 = reqOf(new OrderItemDto(product2.getId(), product2.getProductName(), product2.getPrice(), "M", "BLACK", 2));
        orderService.saveOrder(r1);
        orderService.saveOrder(r2);

        // when
        Page<OrderWithProductsResponse> page = orderService.getAllOrdersByUserId(user1.getId(), 0, 10);

        // then
        assertThat(page.getTotalElements()).isEqualTo(2);
        OrderWithProductsResponse first = page.getContent().get(0);
        assertThat(first.getOrderNumber()).isNotBlank();
        assertThat(first.getOrderStatus()).isEqualTo(StatusEnum.ORDER_WAITING);
        assertThat(first.getItems()).isNotEmpty();
        OrderWithProductsResponse.ProductSummary line = first.getItems().get(0);
        assertThat(line.getProductId()).isNotNull();
        assertThat(line.getProductName()).isNotBlank();
        assertThat(line.getQuantity()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void 사용자별_주문내역_없으면_빈페이지() {
        Page<OrderWithProductsResponse> page = orderService.getAllOrdersByUserId(999_999L, 0, 5);
        assertThat(page.getTotalElements()).isZero();
        assertThat(page.getContent()).isEmpty();
    }


}