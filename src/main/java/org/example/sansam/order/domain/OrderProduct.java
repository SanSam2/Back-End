package org.example.sansam.order.domain;


import jakarta.persistence.*;
import lombok.Getter;
import org.example.sansam.order.tmp.TmpProducts;
import org.example.sansam.status.Status;

import java.util.Objects;


@Entity
@Table(name = "order_product")
@Getter
public class OrderProduct {

    @Id
    @Column(name="order_product_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; //많이 적재되는 테이블에 Long이 맞을까???

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "products_id",nullable = false)
    private TmpProducts product;

    private int quantity;

    private Long canceledQuantity=0L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id", nullable = false)
    private Status status;

    protected OrderProduct(){

    }

    private OrderProduct(Order order, TmpProducts product, int quantity, Status status){
        this.order = order;
        this.product = product;
        this.quantity = quantity;
        this.canceledQuantity=0L;
        this.status = status;
    }

    public static OrderProduct create(Order order, TmpProducts product, int quantity,Status status){
        return new OrderProduct(order,product,quantity,status);
    }

    public void cancelQuantity(int amount, Status canceledStatus, Status partialCanceledStatus){
        if(amount<=0)
            throw new IllegalArgumentException("amount must be greater than 0");

        this.canceledQuantity +=amount;
        if(Objects.equals(this.canceledQuantity, this.quantity)){
            this.status = canceledStatus;
        } else {
            this.status = partialCanceledStatus;
        }
    }

    public void updateOrderProductStatus(Status status){
        this.status = status;
    }



}