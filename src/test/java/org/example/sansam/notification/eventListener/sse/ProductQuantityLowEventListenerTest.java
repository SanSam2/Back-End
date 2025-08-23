package org.example.sansam.notification.eventListener.sse;

import org.example.sansam.cart.repository.CartJpaRepository;
import org.example.sansam.notification.event.sse.ProductQuantityLowEvent;
import org.example.sansam.notification.service.NotificationService;
import org.example.sansam.product.domain.Category;
import org.example.sansam.product.domain.Product;
import org.example.sansam.product.domain.ProductDetail;
import org.example.sansam.s3.domain.FileManagement;
import org.example.sansam.status.domain.Status;
import org.example.sansam.status.domain.StatusEnum;
import org.example.sansam.user.domain.Role;
import org.example.sansam.user.domain.User;
import org.example.sansam.wish.repository.WishJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Stack;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class ProductQuantityLowEventListenerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private CartJpaRepository cartJpaRepository;

    @Mock
    private WishJpaRepository wishJpaRepository;

    @InjectMocks
    private ProductQuantityLowEventListener productQuantityLowEventListener;

    private Product createProduct() {
        Category category = new Category();
        category.setBigName("의류");
        category.setMiddleName("상의");
        category.setSmallName("반팔");

        Status status = new Status(StatusEnum.AVAILABLE);

        FileManagement fileManagement = FileManagement.builder()
                .typeName("PRODUCT_IMAGE")
                .build();

        Product product = new Product();
        product.setCategory(category);
        product.setBrandName("Nike");
        product.setProductName("운동화");
        product.setStatus(status);
        product.setPrice(120000L);
        product.setViewCount(0L);
        product.setDescription("편안한 착용감의 러닝화");
        product.setFileManagement(fileManagement);

        return product;
    }

    private ProductDetail createProductDetail(Product product){
        FileManagement detailFile = FileManagement.builder()
                .id(2L)
                .typeName("PRODUCT_DETAIL_IMAGE")
                .build();

        ProductDetail detail = new ProductDetail();
        detail.setId(10L);
        detail.setProduct(product);
        detail.setQuantity(50L);
        detail.setMapName("블랙/L");
        detail.setVersion(0L);
        detail.setFileManagement(detailFile);

        return detail;
    }
    private User createUser1() {
        return User.builder()
                .id(1L)
                .email("zm@gmail.com")
                .name("테스트1")
                .password("1004")
                .mobileNumber("01012345678")
                .role(Role.USER)
                .salary(10000000L)
                .createdAt(LocalDateTime.now())
                .activated(true)
                .emailAgree(true)
                .build();
    }
    private User createUser2() {
        return User.builder()
                .id(2L)
                .email("zm@naver.com")
                .name("테스트2")
                .password("1004")
                .mobileNumber("01012345678")
                .role(Role.USER)
                .salary(100000L)
                .createdAt(LocalDateTime.now())
                .activated(true)
                .emailAgree(true)
                .build();
    }

    @DisplayName("장바구니에 담은 상품이 재고가 품절 임박 돼서 그 알림을 본인 장바구니에 그 상품을 담은 사용자에게 전송")
    @Test
    void sendNotificationToCartMemberWhenProductQuantityLow(){
        // given
        Product product = createProduct();
        ProductDetail detail = createProductDetail(product);
        ProductQuantityLowEvent event = new ProductQuantityLowEvent(detail);

        User user1 = createUser1();
        User user2 = createUser2();

        when(cartJpaRepository.findUsersByProductDetail_Id(detail.getId()))
                .thenReturn(List.of(user1, user2));
        when(wishJpaRepository.findUsersByProduct_Id(product.getId()))
                .thenReturn(List.of());

        // when
        productQuantityLowEventListener.handleProductQuantityLowEvent(event);

        // then
        verify(notificationService, times(1))
                .sendCartLowNotification(user1, "운동화");
        verify(notificationService, times(1))
                .sendCartLowNotification(user2, "운동화");
        verify(notificationService, never())
                .sendWishListLowNotification(any(), anyString());
    }

    @DisplayName("위시리스트에 담은 상품이 재고가 줄어들어 품절 임박 알림을 전송")
    @Test
    void sendNotificationToWishMemberWhenProductQuantityLow(){
        // given
        Product product = createProduct();
        ProductDetail detail = createProductDetail(product);
        ProductQuantityLowEvent event = new ProductQuantityLowEvent(detail);

        User user = createUser1();

        when(cartJpaRepository.findUsersByProductDetail_Id(detail.getId()))
                .thenReturn(List.of());
        when(wishJpaRepository.findUsersByProduct_Id(product.getId()))
                .thenReturn(List.of(user));

        // when
        productQuantityLowEventListener.handleProductQuantityLowEvent(event);

        // then
        verify(notificationService, never())
                .sendCartLowNotification(any(), anyString());
        verify(notificationService, times(1))
                .sendWishListLowNotification(user, "운동화");
    }

    @DisplayName("장바구니 / 위시리스트 둘 다 담은 유저한테는 두개의 알림 발송")
    @Test
    void sendNotificationToCartAndWishMemberWhenProductQuantityLow(){
        // given
        Product product = createProduct();
        ProductDetail detail = createProductDetail(product);
        ProductQuantityLowEvent event = new ProductQuantityLowEvent(detail);

        User user = createUser1(); // 같은 유저가 카트 + 위시에 모두 담았다고 가정

        when(cartJpaRepository.findUsersByProductDetail_Id(detail.getId()))
                .thenReturn(List.of(user));
        when(wishJpaRepository.findUsersByProduct_Id(product.getId()))
                .thenReturn(List.of(user));

        // when
        productQuantityLowEventListener.handleProductQuantityLowEvent(event);

        // then
        verify(notificationService, times(1))
                .sendCartLowNotification(user, "운동화");
        verify(notificationService, times(1))
                .sendWishListLowNotification(user, "운동화");
    }

    @DisplayName("장바구니 조회 중 예외가 발생하면 이벤트 처리도 예외 발생")
    @Test
    void cartRepositoryThrowsException(){
        // given
        Product product = createProduct();
        ProductDetail detail = createProductDetail(product);
        ProductQuantityLowEvent event = new ProductQuantityLowEvent(detail);

        when(cartJpaRepository.findUsersByProductDetail_Id(detail.getId()))
                .thenThrow(new RuntimeException("DB 오류"));

        // when & then
        assertThrows(RuntimeException.class,
                () -> productQuantityLowEventListener.handleProductQuantityLowEvent(event));
    }

    @DisplayName("위시리스트 조회 중 예외가 발생하면 이벤트 처리도 예외 발생")
    @Test
    void wishRepositoryThrowsException(){
        // given
        Product product = createProduct();
        ProductDetail detail = createProductDetail(product);
        ProductQuantityLowEvent event = new ProductQuantityLowEvent(detail);

        when(cartJpaRepository.findUsersByProductDetail_Id(detail.getId()))
                .thenReturn(List.of());
        when(wishJpaRepository.findUsersByProduct_Id(product.getId()))
                .thenThrow(new RuntimeException("DB 오류"));

        // when & then
        assertThrows(RuntimeException.class,
                () -> productQuantityLowEventListener.handleProductQuantityLowEvent(event));
    }

    @DisplayName("장바구니 알림 전송 중 예외 발생 시 전체 이벤트 처리도 예외 발생")
    @Test
    void cartNotificationThrowsException(){
        // given
        Product product = createProduct();
        ProductDetail detail = createProductDetail(product);
        ProductQuantityLowEvent event = new ProductQuantityLowEvent(detail);

        User user = createUser1();

        when(cartJpaRepository.findUsersByProductDetail_Id(detail.getId()))
                .thenReturn(List.of(user));

        doThrow(new RuntimeException("알림 오류"))
                .when(notificationService).sendCartLowNotification(user, "운동화");

        // when
        productQuantityLowEventListener.handleProductQuantityLowEvent(event);

        // then
        verify(notificationService, times(1))
                .sendCartLowNotification(user, "운동화");
    }
    @DisplayName("위시리스트 알림 전송 중 예외 발생 시 전체 이벤트 처리도 예외 발생")
    @Test
    void wishNotificationThrowsException(){
        // given
        Product product = createProduct();
        ProductDetail detail = createProductDetail(product);
        ProductQuantityLowEvent event = new ProductQuantityLowEvent(detail);

        User user = createUser1();

        when(cartJpaRepository.findUsersByProductDetail_Id(detail.getId()))
                .thenReturn(List.of());
        when(wishJpaRepository.findUsersByProduct_Id(product.getId()))
                .thenReturn(List.of(user));

        doThrow(new RuntimeException("알림 오류"))
                .when(notificationService).sendWishListLowNotification(user, "운동화");

        // when
        productQuantityLowEventListener.handleProductQuantityLowEvent(event);

        // then
        verify(notificationService, times(1))
                .sendWishListLowNotification(user, "운동화");
    }
}