package org.example.sansam.payment.domain;

import jakarta.persistence.*;
import lombok.*;
import org.example.sansam.order.domain.OrderProduct;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "payment_cancellations")
@Getter
@NoArgsConstructor
public class PaymentCancellation {

    @Id
    @Column(name = "payment_cancellations_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_key", nullable = false)
    private String paymentKey;

    @Column(name = "impotency_key")
    private String idempotencyKey;

    @Column(name = "cancel_reason")
    private String cancelReason;

    @Column(name = "order_id", nullable = false)
    private Long orderId; //이건 찐 orderId

    @Column(name = "refund_price")
    private Long refundPrice;

    @Column(name = "cancel_date_time")
    private LocalDateTime cancelDateTime;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "payment_cancellations_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<PaymentCancellationHistory> paymentCancellationHistories = new ArrayList<>();


    private PaymentCancellation(String paymentKey,Long refundPrice,
                                String cancelReason, String idempotencyKey, Long orderId,
                                LocalDateTime cancelDateTime) {
        this.paymentKey = paymentKey;
        this.refundPrice = refundPrice;
        this.cancelReason = cancelReason;
        this.idempotencyKey = idempotencyKey;
        this.orderId = orderId;
        this.cancelDateTime = cancelDateTime;
    }

    public static PaymentCancellation create(String paymentKey, Long refundPrice,
                                       String cancelReason,String idempotencyKey, Long orderId,
                                       LocalDateTime cancelDateTime) {
        return new PaymentCancellation(paymentKey,refundPrice, cancelReason,idempotencyKey,orderId, cancelDateTime);
    }

    public void addCancellationHistory(PaymentCancellationHistory cancelHistory){
        this.paymentCancellationHistories.add(cancelHistory);
    }

}