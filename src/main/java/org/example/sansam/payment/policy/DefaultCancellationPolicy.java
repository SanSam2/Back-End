package org.example.sansam.payment.policy;

import org.example.sansam.exception.pay.CustomException;
import org.example.sansam.exception.pay.ErrorCode;
import org.example.sansam.order.domain.Order;
import org.example.sansam.order.domain.OrderProduct;
import org.example.sansam.payment.dto.CancelProductRequest;
import org.example.sansam.status.domain.StatusEnum;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultCancellationPolicy implements CancellationPolicy{


    private static final Set<StatusEnum> BLOCKED_CANCEL_STATUSES= EnumSet.of(
            StatusEnum.ORDER_PRODUCT_PAID_AND_REVIEW_COMPLETED,
            StatusEnum.ORDER_PRODUCT_CANCELED,
            StatusEnum.ORDER_ALL_CANCELED
    );

    private static final Set<StatusEnum> BLOCKED_CANCEL_PRODUCT_STATUSES= EnumSet.of(
            StatusEnum.ORDER_PRODUCT_PAID_AND_REVIEW_COMPLETED,
            StatusEnum.ORDER_PRODUCT_CANCELED
    );

    private Map<Long, OrderProduct> mapById(List<OrderProduct> ops) {
        Map<Long, OrderProduct> map = new ConcurrentHashMap<>(ops.size());
        for (OrderProduct op : ops) {
            map.put(op.getId(), op);
        }
        return map;
    }

    @Override
    public void validate(Order order, List<CancelProductRequest> cancelProductRequests) {
        if(order == null){
            throw new CustomException(ErrorCode.ORDER_NOT_FOUND);
        }

        if(cancelProductRequests.isEmpty()){
            throw new CustomException(ErrorCode.CANCEL_NOT_FOUND);
        }

        //취소불가한 주문상태이면 저리가!
        StatusEnum orderStatus = order.getStatus().getStatusName();
        if(BLOCKED_CANCEL_STATUSES.contains(orderStatus)){
            throw new CustomException(ErrorCode.ORDER_NOT_CANCELABLE);
        }

        //취소 기간 제한
        LocalDateTime baseTime = order.getCreatedAt();
        if(baseTime != null && Duration.between(baseTime,LocalDateTime.now()).toHours() > 24){
            throw new CustomException(ErrorCode.ORDER_NOT_CANCELABLE);
        }

        Map<Long, Integer> requestedProductByOrderProductId = new ConcurrentHashMap<>();
        for(CancelProductRequest request : cancelProductRequests){
            //orderProduct취소 검증
            if(request.getOrderProductId() == null ){
                throw new CustomException(ErrorCode.INVALID_REQUEST);
            }
            if(request.getCancelQuantity()<=0){
                throw new CustomException(ErrorCode.INVALID_REQUEST);
            }
            requestedProductByOrderProductId.merge(request.getOrderProductId(),request.getCancelQuantity(),Integer::sum);
        }

        //주문으로 함께있는 자식들 다 불러오기
        Map<Long,OrderProduct> orderProductMap = mapById(order.getOrderProducts());

        for(Map.Entry<Long,Integer> entry : requestedProductByOrderProductId.entrySet()){
            Long orderProductId = entry.getKey();
            Integer cancelQuantity = entry.getValue();

            OrderProduct op = orderProductMap.get(orderProductId);
            if(op == null){
                throw new CustomException(ErrorCode.ORDER_PRODUCT_NOT_BELONGS_TO_ORDER);
            }

            StatusEnum opStatus = op.getStatus().getStatusName();
            if(BLOCKED_CANCEL_PRODUCT_STATUSES.contains(opStatus)){
                throw new CustomException(ErrorCode.ORDER_NOT_CANCELABLE);
            }
            long availableQuantity = op.getQuantity() - op.getCanceledQuantity();
            if(cancelQuantity > availableQuantity){
                throw new CustomException(ErrorCode.NOT_ENOUGH_STOCK);
            }
        }
    }
}
