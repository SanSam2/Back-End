package org.example.sansam.payment.compensation.domain;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "payment_cancel_outbox",
        indexes = {
                @Index(name="idx_pco_next_run", columnList = "nextRunAt"),
                @Index(name="idx_pco_status",   columnList = "status")
        },
        uniqueConstraints = {
                @UniqueConstraint(name="uk_pco_idem", columnNames={"idempotencyKey"})
        })
@NoArgsConstructor
public class PaymentCancelOutBox {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private String paymentKey;

    @Column(nullable=false)
    private Long amount;

    @Column(nullable=false, length=200)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    private OutBoxStatus status;

    @Column(nullable=false)
    private int attempt;

    @Column(nullable=false)
    private int maxAttempt;     // 최대 시도

    @Column(nullable=false)
    private LocalDateTime nextRunAt;

    @Column(length=200)
    private String lockedBy;        // 워커 아이디

    private LocalDateTime lockedAt;

    @Column(length=500) private String lastError;

    @Column(nullable=false)
    private LocalDateTime createdAt;

    @Column(nullable=false)
    private LocalDateTime updatedAt;

    @Column(length=64)
    private String idempotencyKey;

    public static PaymentCancelOutBox create(String paymentKey, long amount, String reason, String idempotencyKey) {
        PaymentCancelOutBox o = new PaymentCancelOutBox();
        o.paymentKey = paymentKey;
        o.amount = amount;
        o.reason = reason;
        o.status = OutBoxStatus.PENDING;
        o.attempt = 0;
        o.maxAttempt = 7;
        o.nextRunAt = LocalDateTime.now(); // 즉시 실행 가능
        o.createdAt = LocalDateTime.now();
        o.updatedAt = LocalDateTime.now();
        o.idempotencyKey = idempotencyKey;
        return o;
    }

    public void markClaimed(String workerId) {
        this.status = OutBoxStatus.CLAIMED;
        this.lockedBy = workerId;
        this.lockedAt  = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void markSucceeded() {
        this.status = OutBoxStatus.SUCCEEDED;
        this.updatedAt = LocalDateTime.now();
    }

    public void markFailedAndScheduleRetry(String errorMsg) {
        this.attempt++;
        this.lastError = errorMsg;
        this.updatedAt = LocalDateTime.now();
        if (this.attempt >= this.maxAttempt) {
            this.status = OutBoxStatus.FAILED;
        } else {
            this.status = OutBoxStatus.PENDING;
            long minutes = (long) Math.pow(2, Math.min(this.attempt, 10));
            this.nextRunAt = LocalDateTime.now().plusMinutes(minutes);
            this.lockedBy = null;
            this.lockedAt = null;
        }
    }
}
