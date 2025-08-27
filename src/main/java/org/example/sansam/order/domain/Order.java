package org.example.sansam.order.domain;


import jakarta.persistence.*;
import lombok.Getter;
import org.example.sansam.status.domain.Status;
import org.example.sansam.user.domain.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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


    private String paymentKey;
    private Long totalAmount;

    private LocalDateTime createdAt;
    private LocalDateTime deliveredAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id", nullable = false)
    private Status status;


    @OneToMany(mappedBy = "order",cascade = CascadeType.ALL)
    private List<OrderProduct> orderProducts = new ArrayList<>();

    protected Order() {
        // JPA getDeclaredConstructor().newInstance() 메서드를 통하여 객체를 만들어내기 위해 사용하는 생성자
    }

    private Order(User user, String orderName, String orderNumber, Status status, Long totalAmount, LocalDateTime createdAt) {
        this.user = user;
        this.orderName = orderName;
        this.orderNumber = orderNumber;
        this.status = status;
        this.totalAmount = totalAmount;
        this.createdAt = createdAt;
    }


    public void changeStatus(Status status){
        this.status = status;
    }
    public void addPaymentKey(String paymentKey){
        this.paymentKey = paymentKey;
    }

    public static Order create(User user, String orderName, String orderNumber,
                               Status status, Long totalAmount, LocalDateTime createdAt){
        return new Order(user,orderName,orderNumber,status,totalAmount,createdAt);
    }

    public boolean isAllCanceled() {
        return orderProducts.stream()
                .allMatch(op -> "CANCELED".equals(op.getStatus().getStatusName()));
    }

    public boolean isPartiallyCanceled() {
        return orderProducts.stream()
                .anyMatch(op -> "PARTIAL_CANCELED".equals(op.getStatus().getStatusName()));
    }




    public void completePayment(Status orderPaid, Status orderProductPaid, String paymentKey) {
        this.changeStatus(orderPaid);
        this.addPaymentKey(paymentKey);
        for (OrderProduct op : this.getOrderProducts()) {
            op.updateOrderProductStatus(orderProductPaid);
        }
    }

}
