package org.example.sansam.payment.Mapper;

import org.example.sansam.order.domain.Order;
import org.example.sansam.payment.domain.PaymentMethodType;
import org.example.sansam.payment.domain.Payments;
import org.example.sansam.payment.dto.TossPaymentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentMapper {

    @Mappings({
            @Mapping(target = "method",        expression = "java(mapMethod(payment))"),
            @Mapping(target = "totalAmount",   source = "totalPrice"),
            @Mapping(target = "finalAmount", source = "finalPrice"),
            @Mapping(target = "approvedAt",    source = "approvedAt")
    })
    TossPaymentResponse toTossPaymentResponse(Payments payment);

    default String mapMethod(Payments payment) {
        if (payment == null || payment.getPaymentsType() == null) return null;
        PaymentMethodType type = payment.getPaymentsType().getTypeName();
        if (type == null) return null;
        try {
            return type.toKorean();
        } catch (Throwable ignore) {
            return type.name();
        }
    }

}
