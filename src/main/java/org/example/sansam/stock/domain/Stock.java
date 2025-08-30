package org.example.sansam.stock.domain;


import jakarta.persistence.*;
import lombok.Getter;
import org.example.sansam.exception.pay.CustomException;
import org.example.sansam.exception.pay.ErrorCode;


@Entity
@Table(name = "stock")
@Getter
public class Stock {

    @Id
    @Column(name="stock_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stock_quantity", nullable = false)
    private int stockQuantity;

    @Column(name = "product_details_id", nullable = false)
    private Long productDetailsId;


    protected Stock(){
        //JPA only
    }

    public void decrease(int quantity){
        if(this.stockQuantity < quantity){
            throw new CustomException(ErrorCode.NOT_ENOUGH_STOCK);
        }
        this.stockQuantity -= quantity;
    }

    public void increase(int quantity){
        this.stockQuantity += quantity;
    }


}
