package org.example.sansam.order.domain;


import jakarta.persistence.*;
import lombok.Getter;
import org.example.sansam.exception.pay.CustomException;
import org.example.sansam.exception.pay.ErrorCode;
import org.example.sansam.order.domain.nameformatter.OrderNameFormatter;
import org.example.sansam.order.domain.ordernumber.OrderNumberPolicy;
import org.example.sansam.order.domain.pricing.PricingPolicy;
import org.example.sansam.status.domain.Status;
import org.example.sansam.status.domain.StatusEnum;
import org.example.sansam.user.domain.User;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "orders")
@Getter
public class Order {

    @Id
    @Column(name="orders_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch= FetchType.LAZY)
    @JoinColumn(name="user_id",nullable = false)
    private User user;


    private String orderName;
    private String orderNumber;
    private Long totalAmount;
    private String paymentKey;

    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id", nullable = false)
    private Status status;


    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "order_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<OrderProduct> orderProducts = new ArrayList<>();

    protected Order() {
        // JPA getDeclaredConstructor().newInstance() 메서드를 통하여 객체를 만들어내기 위해 사용하는 생성자
    }

    private Order(User user, Status status,OrderNumberPolicy policy,LocalDateTime createdAt) {
        this.user = user;
        this.status = status;
        addOrderNumber(policy);
        this.createdAt = createdAt;
    }

    public static Order create(User user,
                               Status status,OrderNumberPolicy policy, LocalDateTime createdAt) {
        return new Order(user, status, policy, createdAt);
    }

    protected void addOrderNumber(OrderNumberPolicy policy) {
        this.orderNumber = policy.makeOrderNumber();
    }

    public void addOrderProduct(OrderProduct orderProduct){
        this.orderProducts.add(orderProduct);
    }

    public void addOrderName(OrderNameFormatter f){
        this.orderName = buildOrderName(f);
    }

    private String buildOrderName(OrderNameFormatter f){
        int count = orderProducts.size();
        if(count == 0){
            throw new CustomException(ErrorCode.NO_ITEM_IN_ORDER);
        }
        String first = orderProducts.get(0).getProduct().getProductName();
        return f.format(first,count-1);
    }

    public void calcTotal(PricingPolicy policy) {
         this.totalAmount = policy.totalOf(this.orderProducts);
    }

    public void addPaymentKey(String paymenKey){
        this.paymentKey = paymenKey;
    }

    private void changeStatus(Status status){
        this.status = status;
    }

    public void changeStatusMarkExpired(Status orderExpired){
        changeStatus(orderExpired);
    }

    public void changeStatusWhenCompletePayment(Status orderPaid, Status orderProductPaid) {
        if(orderProducts.isEmpty()){
            throw new CustomException(ErrorCode.NO_ITEM_IN_ORDER);
        }
        this.changeStatus(orderPaid);
        for (OrderProduct op : orderProducts) {
            op.updateOrderProductStatus(orderProductPaid);
        }
    }

    //취소 후 얘 하나만 체크하면 되니까
    public void changeStatusAfterItemCancellation(Status allCanceled, Status partialCanceled) {
        if (orderProducts == null || orderProducts.isEmpty())
            throw new CustomException(ErrorCode.NO_ITEM_IN_ORDER);

        if (areAllItemsCanceled()) {
            changeStatus(allCanceled);
        } else if (anyItemCanceled()) {
            changeStatus(partialCanceled);
        }
    }

    private boolean areAllItemsCanceled() {
        return !orderProducts.isEmpty() &&
                orderProducts.stream()
                        .allMatch(op -> ITEM_CANCELED_STATES.contains(op.getStatus().getStatusName()));
    }

    private boolean anyItemCanceled() {
        return orderProducts.stream()
                .anyMatch(op -> ITEM_CANCELED_STATES.contains(op.getStatus().getStatusName()));
    }

    // 어떤 상태를 "아이템 취소"로 볼지
    private static final Set<StatusEnum> ITEM_CANCELED_STATES = EnumSet.of(
            StatusEnum.ORDER_PRODUCT_CANCELED
    );

    // "주문 레벨"에서 취소로 간주할 상태들
    private static final Set<StatusEnum> ORDER_LEVEL_CANCELED_STATES = EnumSet.of(
            StatusEnum.ORDER_ALL_CANCELED,
            StatusEnum.ORDER_PARTIAL_CANCELED
    );

}
