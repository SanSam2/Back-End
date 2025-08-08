package org.example.sansam.payment.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.sansam.status.Status;

@Entity
@Table(name = "payment_cancellations_histories")
@Getter
@NoArgsConstructor
public class PaymentCancellationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_cancellations_histories_id")
    private Long id;

    @Column(name = "order_product_id", nullable = false)
    private Long orderProductId;

    @Column(name = "quantity")
    private int quantity; //총 취소 수량 몇개 취소되었는지

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id", nullable = false)
    private Status status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_cancellations_id")
    private PaymentCancellation paymentCancellation;

    private PaymentCancellationHistory(Long orderProductId, int quantity, Status status) {
        this.orderProductId = orderProductId;
        this.quantity = quantity;
        this.status = status;
    }

    public static PaymentCancellationHistory create(Long orderProductId, int quantity, Status status){
        return new PaymentCancellationHistory(orderProductId, quantity, status);
    }

    public void changeStatus(Status status){
        this.status = status;
    }

    public void setPaymentCancellation(PaymentCancellation paymentCancellation) {
        this.paymentCancellation = paymentCancellation;
    }


}