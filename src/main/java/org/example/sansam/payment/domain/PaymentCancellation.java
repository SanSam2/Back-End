package org.example.sansam.payment.domain;

import jakarta.persistence.*;
import lombok.*;

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

    @Column(name = "cancel_amount")
    private int cancelAmount;

    @Column(name = "cancel_reason")
    private String cancelReason;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "refund_price")
    private Long refundPrice;

    @Column(name = "cancel_date_time")
    private LocalDateTime cancelDateTime;

    @OneToMany(mappedBy = "paymentCancellation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PaymentCancellationHistory> histories = new ArrayList<>();


    private PaymentCancellation(String paymentKey, int cancelAmount,Long refundPrice,
                                String cancelReason, Long orderId,
                                LocalDateTime cancelDateTime) {
        this.paymentKey = paymentKey;
        this.cancelAmount = cancelAmount;
        this.refundPrice = refundPrice;
        this.cancelReason = cancelReason;
        this.orderId = orderId;
        this.cancelDateTime = cancelDateTime;
    }

    public static PaymentCancellation create(String paymentKey, int cancelAmount, Long refundPrice,
                                       String cancelReason, Long orderId,
                                       LocalDateTime cancelDateTime) {
        return new PaymentCancellation(paymentKey, cancelAmount,refundPrice, cancelReason, orderId, cancelDateTime);
    }

    public void addHistory(PaymentCancellationHistory history) {
        histories.add(history);
        history.setPaymentCancellation(this);
    }

    //혹시나 여러개가 한번에 생성될 수가 있기 때문에.
    public void addHistories(List<PaymentCancellationHistory> historyList) {
        for (PaymentCancellationHistory history : historyList) {
            addHistory(history);
        }
    }


}