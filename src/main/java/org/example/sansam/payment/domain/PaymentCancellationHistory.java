package org.example.sansam.payment.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.sansam.status.domain.Status;

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
    private Status status; //Cancel_refunded = pg,결제단 정산 확정 / Cancel_Completed 부가 후처리까지 완료

    private PaymentCancellationHistory(Long orderProductId, int quantity, Status status) {
        this.orderProductId = orderProductId;
        this.quantity = quantity;
        this.status = status;
    }

    public static PaymentCancellationHistory create(Long orderProductId, int quantity, Status status){
        return new PaymentCancellationHistory(orderProductId, quantity, status);
    }

    private void changeStatus(Status status){
        this.status = status;
    }

    //Pg나 결제단에서 정산이 확정
    public void changeStatusWhenCompleteCancel(Status status){
        this.changeStatus(status);
    }

    //CancelCompleted 부가 후처리까지 완료된 경우 사용
    public void changeStatusWhenCompleteRefund(Status status){
        this.changeStatus(status);
    }




}