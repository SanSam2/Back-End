package org.example.sansam.order.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.sansam.order.domain.Order;
import org.example.sansam.order.domain.OrderProduct;
import org.example.sansam.order.domain.QOrder;
import org.example.sansam.order.domain.QOrderProduct;
import org.example.sansam.product.domain.QProduct;
import org.example.sansam.status.domain.QStatus;
import org.example.sansam.status.domain.Status;
import org.example.sansam.user.domain.QUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class OrderRepositoryImpl implements OrderRepositoryCustom{

    private final JPAQueryFactory queryFactory;
    private final EntityManager em;

    public OrderRepositoryImpl(EntityManager em) {
        this.em = em;
        this.queryFactory = new JPAQueryFactory(em);
    }

    private static final QOrder o = QOrder.order;
    private static final QUser u = QUser.user;
    private static final QStatus s = QStatus.status;
    private static final QProduct p = QProduct.product;
    private static final QStatus os=new QStatus("os");
    private static final QStatus ops = new QStatus("ops");

    private static final QOrderProduct op = QOrderProduct.orderProduct;


    @Override
    public List<Order> searchOrderByUserId(Long userId) {
        return queryFactory
                .selectFrom(o)
                .join(o.user,u).fetchJoin()
                .join(o.status,s).fetchJoin()
                .where(o.user.id.eq(userId))
                .orderBy(o.createdAt.desc(),o.id.desc())
                .fetch();
    }

    @Override
    public Order findOrderByOrderNumber(String orderNumber) {
        return queryFactory
                .selectFrom(o)
                .distinct()
                .leftJoin(o.orderProducts,op).fetchJoin()
                .where(o.orderNumber.eq(orderNumber))
                .fetchOne();
    }

    @Override
    public Optional<OrderProduct> findReviewTarget(Long userId, String orderNumber, Long orderProductId) {
        OrderProduct row = queryFactory
                .select(op)
                .from(o)
                .join(o.orderProducts,op)
                .join(o.status,os)
                .join(op.status,ops)
                .leftJoin(op.product,p).fetchJoin()
                .where(
                        o.orderNumber.eq(orderNumber),
                        userIdEq(userId),
                        op.id.eq(orderProductId)
                )
                .fetchOne();

        return Optional.ofNullable(row);
    }

    @Override
    public Page<Long> pageOrderIdsByUserId(Long userId, Pageable pageable) {
        List<Long> ids = queryFactory
                .select(o.id)
                .from(o)
                .where(o.user.id.eq(userId))
                .orderBy(o.createdAt.desc(), o.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(o.id.count())
                .from(o)
                .where(o.user.id.eq(userId))
                .fetchOne();

        return new PageImpl<>(ids, pageable, Objects.requireNonNull(total));
    }

    @Override
    public List<Order> findOrdersWithItemsByIds(List<Long> ids) {
        QStatus os = new QStatus("os");
        QStatus ops = new QStatus("ops");
        return queryFactory
                .selectFrom(o).distinct()
                .join(o.status, os).fetchJoin()
                .leftJoin(o.orderProducts, op).fetchJoin()
                .leftJoin(op.status, ops).fetchJoin()
                .leftJoin(op.product, p).fetchJoin()
                .where(o.id.in(ids))
                .fetch();
    }

    @Override
    @Transactional
    public int deleteExpiredWaitingOrders(Status status, LocalDateTime expiredAt) {
        long deleted = queryFactory
                .delete(o)
                .where(o.status.eq(status),
                        o.createdAt.lt(expiredAt))
                .execute();

        em.flush();
        em.clear();

        return (int) deleted;
    }

    private BooleanExpression userIdEq(Long userId){
        return o.user.id.eq(userId);
    }

    public List<Long> findExpiredWaitingOrderIds(Status waiting, LocalDateTime expiredAt, int limit) {
        return queryFactory
                .select(o.id)
                .from(o)
                .where(o.status.eq(waiting), o.createdAt.lt(expiredAt))
                .orderBy(o.createdAt.asc(), o.id.asc())
                .limit(limit)
                .fetch();
    }

    public Optional<Order> findByIdWithItems(Long id) {
        return Optional.ofNullable(queryFactory
                .selectFrom(o)
                .leftJoin(o.orderProducts, op).fetchJoin()
                .where(o.id.eq(id))
                .fetchOne());
    }

}
