package org.example.sansam.payment.domain;


import jakarta.persistence.*;
import lombok.*;
import org.example.sansam.exception.pay.CustomException;
import org.example.sansam.exception.pay.ErrorCode;
import org.example.sansam.order.domain.Order;
import org.example.sansam.status.domain.Status;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor
@AllArgsConstructor
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
    private PaymentsType paymentsType;

    @Column(name="payment_key")
    private String paymentKey;

    @Column(name="total_price")
    private Long totalPrice; // 불변하는 진짜 최종 결제 금액

    @Column(name = "final_price")
    private Long finalPrice; // 남은 금액 = 취소할 수 있는 금액(잔고)

    @Column(name="requested_at", columnDefinition = "DATETIME(6)")
    private LocalDateTime requestedAt;

    @Column(name="approved_at", columnDefinition = "DATETIME(6)")
    private LocalDateTime approvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id", nullable = false)
    private Status status;

    private Payments(Order order, PaymentsType paymentsType, String paymentKey, Long totalPrice,Long finalPrice,
                     LocalDateTime requestedAt, LocalDateTime approvedAt, Status status){
        this.order= order;
        this.paymentsType= paymentsType;
        this.paymentKey= paymentKey;
        this.totalPrice= totalPrice;
        this.finalPrice= finalPrice;
        this.requestedAt= requestedAt;
        this.approvedAt = approvedAt;
        setStatusAfterConfirm(status);
    }

    public static Payments create(Order order, PaymentsType paymentsType,String paymentKey, Long totalPrice, Long finalPrice,
                                  LocalDateTime requestedAt, LocalDateTime approvedAt, Status status){
        if(order==null || paymentsType ==null ||paymentKey==null|| totalPrice==null ||requestedAt==null || approvedAt==null){
            throw new CustomException(ErrorCode.PAYMENT_REQUIRE_ABSCENT);
        }else{
            return new Payments(order, paymentsType, paymentKey, totalPrice,finalPrice, requestedAt, approvedAt,status);
        }
    }

    private void setStatusAfterConfirm(Status status){
        changeStatus(status);
    }

    private void changeStatus(Status status){
        this.status = status;
    }


}
