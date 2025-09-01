package org.example.sansam.order.compensation.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.sansam.payment.compensation.domain.OutBoxStatus;

import java.time.LocalDateTime;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Table(name="stock_restore_outbox",
        indexes = {@Index(name="idx_sro_next", columnList="nextRunAt"),
                @Index(name="idx_sro_status", columnList="status")},
        uniqueConstraints = {
                @UniqueConstraint(name="uk_sro_idem", columnNames={"idempotencyKey"})
        })
@Getter
@NoArgsConstructor
public class StockRestoreOutBox {
    @Id @GeneratedValue(strategy=IDENTITY)
    Long id;

    @Column(nullable=false)
    Long orderId;

    @Column(nullable=false)
    Long productDetailId;

    @Column(nullable=false)
    Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    OutBoxStatus status;

    @Column(nullable=false)
    Integer attempt;

    @Column(nullable=false)
    Integer maxAttempt;

    @Column(nullable=false)
    LocalDateTime nextRunAt;

    String lockedBy;

    LocalDateTime lockedAt;

    String lastError;

    LocalDateTime createdAt;

    LocalDateTime updatedAt;

    @Column(length=100)
    String idempotencyKey;

    public static StockRestoreOutBox create(Long orderId, Long detailId, int qty, String idemKey) {
        StockRestoreOutBox o = new StockRestoreOutBox();
        o.orderId = orderId;
        o.productDetailId = detailId;
        o.quantity = qty;
        o.status = OutBoxStatus.PENDING;
        o.attempt = 0; o.maxAttempt = 7;
        o.nextRunAt = LocalDateTime.now();
        o.createdAt = LocalDateTime.now(); o.updatedAt = LocalDateTime.now();
        o.idempotencyKey = idemKey;
        return o;
    }
    public void markSucceeded(){
        this.status=OutBoxStatus.SUCCEEDED; this.updatedAt=LocalDateTime.now();
    }

    public void markFailedAndScheduleRetry(String msg){
        this.attempt++; this.lastError=msg; this.updatedAt=LocalDateTime.now();
        if(this.attempt>=this.maxAttempt){
            this.status=OutBoxStatus.FAILED;
        }
        else {
            this.status=OutBoxStatus.PENDING; this.nextRunAt=LocalDateTime.now().plusMinutes((long)Math.pow(2, Math.min(this.attempt,10)));
            this.lockedBy=null; this.lockedAt=null;
        }
    }
}

