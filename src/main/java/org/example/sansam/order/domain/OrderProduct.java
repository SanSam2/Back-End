package org.example.sansam.order.domain;


import jakarta.persistence.*;
import lombok.Getter;
import org.example.sansam.exception.pay.CustomException;
import org.example.sansam.exception.pay.ErrorCode;
import org.example.sansam.product.domain.Product;
import org.example.sansam.status.domain.Status;
import org.example.sansam.status.domain.StatusEnum;


@Entity
@Table(name = "order_product")
@Getter
public class OrderProduct {

    @Id
    @Column(name="order_product_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id",nullable = false)
    private Product product;

    private int quantity; //주문한 상품 수량

    private Long orderedProductPrice;

    private String orderedProductSize;

    private String orderedProductColor;

    private int canceledQuantity=0;

    private String representativeURL;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id", nullable = false)
    private Status status;

    protected OrderProduct(){

    }

    private OrderProduct(Product product, Long orderedProductPrice, int quantity,String orderedProductSize,String orderedproductColor,String representativeURL, Status status){
        this.product = product;
        this.orderedProductPrice = orderedProductPrice;
        this.orderedProductSize = orderedProductSize;
        this.orderedProductColor= orderedproductColor;
        this.quantity = quantity;
        this.canceledQuantity=0;
        this.representativeURL = representativeURL;
        this.status = status;
    }

    public static OrderProduct create(Product product,Long orderedProductPrice, int quantity, String orderedProductSize, String orderedproductColor,String representativeURL, Status status){
        return new OrderProduct(product,orderedProductPrice,quantity,orderedProductSize,orderedproductColor,representativeURL,status);
    }

    //부분취소/전체취소 이후 상태값 변화 가져가기
    public void cancelQuantityCheckChange(int quantity, Status canceledStatus, Status partialCanceledStatus){
        if(quantity<=0){
            throw new CustomException(ErrorCode.CANCEL_QUANTITY_MUST_MORE_THEN_ZERO);
        }else if(quantity>this.quantity){
            throw new CustomException(ErrorCode.CANNOT_CANCEL_MORE_THAN_ORDERED_QUANTITY);
        }
        this.canceledQuantity +=quantity;
        if(this.canceledQuantity==this.quantity){
            updateOrderProductStatus(canceledStatus);
        } else {
            updateOrderProductStatus(partialCanceledStatus);
        }
    }

    public void reviewCompletedStatusChange(){
        updateOrderProductStatus(new Status(StatusEnum.ORDER_PRODUCT_REVIEW_COMPLETED));
    }

    protected void updateOrderProductStatus(Status newStatus){
        if(newStatus==null){
            throw new CustomException(ErrorCode.CHECK_STATUS);
        }
        this.status = newStatus;
    }

}