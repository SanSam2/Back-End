package org.example.sansam.stockreservation.domain;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.Instant;

@Entity
@Getter
@Table(name="stock_reservation")
public class StockReservation {
    @Id
    private String orderId;

    @Enumerated(EnumType.STRING)
    private Status status;

    private String reason;

    private String requestEventId;

    private Instant updatedAt;

    public enum Status {
        PENDING, CONFIRMED, REJECTED
    }

    public static StockReservation pending(String orderId, String reqId){
        StockReservation s = new StockReservation();
        s.orderId = orderId;
        s.status = Status.PENDING;
        s.requestEventId = reqId;
        s.updatedAt = Instant.now();
        return s;
    }
    public void markConfirmed(String reqId){
        this.status=Status.CONFIRMED;
        this.reason=null;
        this.requestEventId=reqId;
        this.updatedAt=Instant.now();
    }

    public void markRejected(String reqId, String reason){
        this.status=Status.REJECTED;
        this.reason=reason;
        this.requestEventId=reqId;
        this.updatedAt=Instant.now();
    }
}