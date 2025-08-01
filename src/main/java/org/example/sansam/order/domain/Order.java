package org.example.sansam.order.domain;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.sansam.order.tmp.OrderStatus;
import org.example.sansam.user.domain.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter @Setter
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch= FetchType.LAZY)
    @JoinColumn(name="user_id",nullable = false)
    private User user;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name="status_id",nullable = false)
//    private OrderStatus status;

    private String orderName;
    private String orderNumber;
    private OrderStatus status;

    private String paymentKey; //프론트랑 협의 해봐야합니다..
    private Long totalAmount;

    private LocalDateTime createdAt;
    private LocalDateTime deliveredAt;

    @OneToMany(mappedBy = "order",cascade = CascadeType.ALL)
    private List<OrderProduct> orderProducts = new ArrayList<>();
}
