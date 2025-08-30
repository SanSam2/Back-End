package org.example.sansam.order.repository;

import jakarta.persistence.EntityManager;
import org.example.sansam.order.domain.Order;
import org.example.sansam.order.domain.OrderProduct;
import org.example.sansam.order.domain.ordernumber.OrderNumberPolicy;
import org.example.sansam.product.domain.Category;
import org.example.sansam.product.domain.Product;
import org.example.sansam.status.domain.Status;
import org.example.sansam.status.domain.StatusEnum;
import org.example.sansam.user.domain.Role;
import org.example.sansam.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class) //DataJpaTest에 이미 내장되어있다.
@DataJpaTest(showSql = true)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private EntityManager em;

    private User user1;
    private User user2;
    private Status orderWaitingStatus;
    private Status orderProductWaiting;
    private Status orderPaid;
    private Product product;

    private static class fakeOrderNumberPolicy implements OrderNumberPolicy {

        @Override
        public String makeOrderNumber() {
            return "1234567890-123e4567-e89b-12d3-a456-426614174000";
        }
    }

    @BeforeEach
    void init() {
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


        Category cat = new Category();
        cat.setBigName("TOPS");
        cat.setMiddleName("TEE");
        cat.setSmallName("BASIC");
        em.persist(cat);

        Status productStatus = new Status(StatusEnum.AVAILABLE);
        em.persist(productStatus);

        product = new Product();
        product.setCategory(cat);
        product.setStatus(productStatus);
        product.setBrandName("NIKE");
        product.setProductName("Air Tee");
        product.setPrice(10000L);
        em.persist(product);

        em.flush();
    }

    @Test
    void JPA가_H2와_제대로_연결되었다_주문저장이_성공하였다(){
        // given
        Order order = Order.create(user1, orderWaitingStatus, new fakeOrderNumberPolicy(), LocalDateTime.now());

        OrderProduct op = OrderProduct.create(
                product, 10000L, 2, "M", "BLACK", "url", orderProductWaiting
        );
        order.addOrderProduct(op); //

        // when
        Order saved = orderRepository.saveAndFlush(order);

        // then
        assertNotNull(saved.getId());
        assertEquals("1234567890-123e4567-e89b-12d3-a456-426614174000", saved.getOrderNumber());
        assertEquals(user1.getId(), saved.getUser().getId());
        assertEquals(orderWaitingStatus.getStatusName(), saved.getStatus().getStatusName());
        assertEquals(1, saved.getOrderProducts().size());
    }

    @Test
    void findByOrderNumber로_orderNumber로_order조회가_가능하다() {
        //given
        Order order = Order.create(user1, orderWaitingStatus, new fakeOrderNumberPolicy(), LocalDateTime.now());
        OrderProduct op = OrderProduct.create(
                product, 10000L, 2, "M", "BLACK", "url", orderProductWaiting
        );
        order.addOrderProduct(op);
        orderRepository.save(order);

        //when
        Order o1 = orderRepository.findByOrderNumber(order.getOrderNumber()).orElseThrow();

        //then
        assertEquals(order.getOrderNumber(), o1.getOrderNumber());
    }

    @Test
    void userID로_user에_해당하는_주문을_뽑을_수_있다() {
        //given
        Order o1 = Order.create(user1, orderWaitingStatus, new fakeOrderNumberPolicy(), LocalDateTime.now());
        Order o2 = Order.create(user1, orderWaitingStatus, new fakeOrderNumberPolicy(), LocalDateTime.now());
        Order o3 = Order.create(user2, orderWaitingStatus, new fakeOrderNumberPolicy(), LocalDateTime.now());

        OrderProduct op1_o1 = OrderProduct.create(product, 10000L, 2, "M", "BLACK", "url", orderProductWaiting);
        OrderProduct op2_o2 = OrderProduct.create(product, 1000000L, 1, "S", "WHITE", "url", orderProductWaiting);
        OrderProduct op1_o3 = OrderProduct.create(product, 10000L, 2, "M", "BLACK", "url", orderProductWaiting);

        o1.addOrderProduct(op1_o1);
        o2.addOrderProduct(op2_o2);
        o3.addOrderProduct(op1_o3);
        orderRepository.save(o1);
        orderRepository.save(o2);
        orderRepository.save(o3);

        //when
        List<Order> result = orderRepository.searchOrderByUserId(user1.getId());

        //then
        assertEquals(2, result.size());
        assertThat(result).allMatch(o -> o.getUser().getId().equals(user1.getId()));
    }

    @Test
    void findOrderProductByOrderNumber로_orderNumber에서_주문을_뽑을_수_있다() {
        //given
        Order order = Order.create(user1, orderWaitingStatus, new fakeOrderNumberPolicy(), LocalDateTime.now());
        OrderProduct op = OrderProduct.create(
                product, 10000L, 2, "M", "BLACK", "url", orderProductWaiting
        );
        order.addOrderProduct(op);
        orderRepository.save(order);

        //when
        Order found = orderRepository.findOrderByOrderNumber("1234567890-123e4567-e89b-12d3-a456-426614174000");

        //then
        assertNotNull(found);
        assertEquals(1, found.getOrderProducts().size());
        assertEquals(op.getProduct().getId(), found.getOrderProducts().get(0).getProduct().getId());
    }

    @Test
    void findOrderProductByOrderNumber_없으면_null반환() {
        Order found = orderRepository.findOrderByOrderNumber("없는-번호");
        assertThat(found).isNull();
    }

    @Test
    void findByOrderNumber_없으면_빈Optional() {
        assertThat(orderRepository.findByOrderNumber("x-없음")).isEmpty();
    }


    @Test
    void searchOrderByUserId_정렬은_createdAt_DESC나_id_DESC로_이루어진다(){
        //given
        LocalDateTime base = LocalDateTime.now().minusHours(1);
        Order o1 = Order.create(user1, orderWaitingStatus, new fakeOrderNumberPolicy(), base);
        o1.addOrderProduct(OrderProduct.create(product, 1L, 1, "M","B","url", orderProductWaiting));
        orderRepository.save(o1);

        Order o2 = Order.create(user1, orderWaitingStatus, new fakeOrderNumberPolicy(), base);
        o2.addOrderProduct(OrderProduct.create(product, 2L, 1, "M","B","url", orderProductWaiting));
        orderRepository.save(o2);

        Order o3 = Order.create(user1, orderWaitingStatus, new fakeOrderNumberPolicy(), base.plusMinutes(1));
        o3.addOrderProduct(OrderProduct.create(product, 1L, 1, "M","B","url", orderProductWaiting));
        orderRepository.save(o3);

        //when
        List<Order> result = orderRepository.searchOrderByUserId(user1.getId());

        //then
        assertThat(result).extracting(Order::getId).containsExactly(o3.getId(),
                Math.max(o1.getId(), o2.getId()),
                Math.min(o1.getId(), o2.getId()));

    }

    @Test
    void findOrderByOrderNumber가_fetchJoin으로_즉시로딩된다(){
        //given
        Order o1 = Order.create(user1, orderWaitingStatus, new fakeOrderNumberPolicy(), LocalDateTime.now());
        o1.addOrderProduct(OrderProduct.create(product, 1L, 1, "M","B","url", orderProductWaiting));
        orderRepository.save(o1);

        //when
        Order found = orderRepository.findOrderByOrderNumber(o1.getOrderNumber());

        //then
        assertThat(found).isNotNull();
        assertThat(found.getOrderProducts()).hasSize(1);
    }

    @Test
    void findReviewTarget으로_성공적으로_한건을_찾는다() {
        //given
        Order order = Order.create(user1, orderPaid, new fakeOrderNumberPolicy(), LocalDateTime.now());
        OrderProduct op = OrderProduct.create(product, 1000L, 1, "M","B","url",
                orderPaid);
        order.addOrderProduct(op);
        orderRepository.save(order);

        //when
        var found = orderRepository.findReviewTarget(user1.getId(), order.getOrderNumber(), op.getId());

        //then
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(op.getId());
        var util = em.getEntityManagerFactory().getPersistenceUnitUtil();
        assertThat(util.isLoaded(found.get(), "product")).isTrue();
    }

    @Test
    void pageOrderIdsByUserId_페이징과_정렬이_정확하다() {
        //given
        LocalDateTime base = LocalDateTime.now();
        for (int i = 0; i < 5; i++) {
            Order o = Order.create(user1, orderWaitingStatus, new fakeOrderNumberPolicy(), base.minusMinutes(i));
            o.addOrderProduct(OrderProduct.create(product, 1L, 1, "M","B","url", orderProductWaiting));
            orderRepository.save(o);
        }

        //when
        Pageable pageable = PageRequest.of(0, 2, Sort.by("createdAt").descending().and(Sort.by("id").descending()));
        Page<Long> page = orderRepository.pageOrderIdsByUserId(user1.getId(), pageable);

        //then
        assertThat(page.getTotalElements()).isEqualTo(5);
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getNumber()).isEqualTo(0);
    }

    @Test
    void pageOrderIdsByUserId_주문없으면_빈페이지() {
        //given & when
        Pageable pageable = PageRequest.of(0, 5);
        Page<Long> page = orderRepository.pageOrderIdsByUserId(999_999L, pageable);
        //then
        assertThat(page.getTotalElements()).isZero();
        assertThat(page.getContent()).isEmpty();
    }

    @Test
    void findOrdersWithItemsByIds에서_빈목록이면_빈리턴이_나온다() {
        //given 없음
        //when & then
        assertThat(orderRepository.findOrdersWithItemsByIds(List.of())).isEmpty();
    }

    @Test
    void findOrdersWithItemsByIds_없는ID는_무시하고_있는것만_반환() {
        //given
        Order a = Order.create(user1, orderWaitingStatus, new fakeOrderNumberPolicy(), LocalDateTime.now());
        a.addOrderProduct(OrderProduct.create(product, 1L, 1, "M","B","url", orderProductWaiting));
        orderRepository.save(a);

        //when
        List<Order> loaded = orderRepository.findOrdersWithItemsByIds(List.of(a.getId(), -1L));

        //then
        assertThat(loaded).extracting(Order::getId).containsExactly(a.getId());
    }

    @Test
    void findOrdersWithItemsByIds_distinct로_중복루트가_사라진다_그리고_연관이_예열된다() {
        //given
        Order a = Order.create(user1, orderWaitingStatus, new fakeOrderNumberPolicy(), LocalDateTime.now());
        a.addOrderProduct(OrderProduct.create(product, 1L, 1, "M","B","url", orderProductWaiting));
        a.addOrderProduct(OrderProduct.create(product, 2L, 2, "L","W","url", orderProductWaiting));
        orderRepository.save(a);

        //when
        List<Order> loaded = orderRepository.findOrdersWithItemsByIds(List.of(a.getId()));

        //then
        assertThat(loaded).hasSize(1);

        //더 자세하게 테스트 한번 더
        Order found = loaded.get(0);
        var util = em.getEntityManagerFactory().getPersistenceUnitUtil();
        assertThat(util.isLoaded(found, "orderProducts")).isTrue();
        assertThat(found.getOrderProducts()).hasSize(2);
        found.getOrderProducts().forEach(line -> {
            assertThat(util.isLoaded(line, "product")).isTrue();
            assertThat(util.isLoaded(line, "status")).isTrue();
        });
    }

    @Test
    void 상태와_시간조건으로만_과거대상_삭제된다() {

        //given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiredAt = now.minusDays(3);

        for (int i = 0; i < 5; i++) {
            Order o = Order.create(user1, orderWaitingStatus, new fakeOrderNumberPolicy(), now.minusDays(5));
            o.addOrderProduct(OrderProduct.create(product, 10000L, 1, "M","BLACK","url", orderWaitingStatus));
            orderRepository.save(o);
        }

        for (int i = 0; i < 2; i++) {
            Order o = Order.create(user1, orderWaitingStatus, new fakeOrderNumberPolicy(), now.minusDays(1));
            o.addOrderProduct(OrderProduct.create(product, 10000L, 1, "M","BLACK","url", orderWaitingStatus));
            orderRepository.save(o);
        }

        //when
        int deleted = orderRepository.deleteExpiredWaitingOrders(orderWaitingStatus, expiredAt);

        //then
        assertThat(deleted).isEqualTo(5);

        var remain = orderRepository.searchOrderByUserId(user1.getId());
        assertThat(remain).noneMatch(o ->
                o.getStatus().getStatusName() == StatusEnum.ORDER_WAITING
                        && o.getCreatedAt().isBefore(expiredAt)
        );

        long recentWaitingCount = remain.stream()
                .filter(o -> o.getStatus().getStatusName() == StatusEnum.ORDER_WAITING
                        && !o.getCreatedAt().isBefore(expiredAt))
                .count();
        assertThat(recentWaitingCount).isEqualTo(2);
    }

    @Test
    void 다른상태는_오래되어도_삭제되지_않는다() {
        // given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiredAt = now.minusDays(3);

        for (int i = 0; i < 3; i++) {
            Order o = Order.create(user1, orderPaid, new fakeOrderNumberPolicy(), now.minusDays(5));
            o.addOrderProduct(OrderProduct.create(product, 10000L, 1, "M","BLACK","url", orderWaitingStatus));
            orderRepository.save(o);
        }

        // when
        int deleted = orderRepository.deleteExpiredWaitingOrders(orderWaitingStatus, expiredAt);

        // then
        assertThat(deleted).isZero();

        var remain = orderRepository.searchOrderByUserId(user1.getId());
        long paidCount = remain.stream()
                .filter(o -> o.getStatus().getStatusName() == StatusEnum.ORDER_PAID)
                .count();
        assertThat(paidCount).isEqualTo(3);
    }


    @Test
    void 경계값expiredAt과_동일시각은_삭제되지_않는다(){
        // given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiredAt = now.minusDays(3);

        Order o = Order.create(user1, orderWaitingStatus, new fakeOrderNumberPolicy(), now.minusDays(3));
        o.addOrderProduct(OrderProduct.create(product, 10000L, 1, "M","BLACK","url", orderWaitingStatus));
        orderRepository.save(o);

        // when
        int deleted = orderRepository.deleteExpiredWaitingOrders(orderWaitingStatus, expiredAt);

        // then
        assertThat(deleted).isZero(); // 이 테스트에서는 boundary만 존재하므로 0
        var remain = orderRepository.searchOrderByUserId(user1.getId());
        assertThat(remain).extracting(Order::getId).contains(o.getId());
        assertThat(remain).filteredOn(order -> order.getId().equals(o.getId()))
                .first()
                .satisfies(order -> {
                    assertThat(o.getStatus().getStatusName()).isEqualTo(StatusEnum.ORDER_WAITING);
                    assertThat(o.getCreatedAt()).isEqualTo(expiredAt);
                });
    }

}
