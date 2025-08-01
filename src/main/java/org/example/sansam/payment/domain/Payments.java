package org.example.sansam.payment.domain;


import jakarta.persistence.*;
import lombok.*;
import org.example.sansam.order.domain.Order;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payments {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name= "payments_id")
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="order_id",nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="payments_type_id",nullable = false)
    private PaymentsType paymnetsType;

    @Column(name="total_price")
    private Long totalPrice;

    @Column(name = "final_price")
    private Long finalPrice;

    @Column(name="created_at")
    private LocalDateTime createdAt;


}
