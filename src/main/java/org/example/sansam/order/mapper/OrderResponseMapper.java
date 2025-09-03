package org.example.sansam.order.mapper;

import org.example.sansam.order.domain.Order;
import org.example.sansam.order.domain.OrderProduct;
import org.example.sansam.order.dto.OrderItemResponseDto;
import org.example.sansam.order.dto.OrderResponse;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderResponseMapper {

    @Mapping(target = "productId",            source = "product.id")
    @Mapping(target = "productName",          source = "product.productName")
    @Mapping(target = "productPrice",         source = "orderedProductPrice")
    @Mapping(target = "productSize",          source = "orderedProductSize")
    @Mapping(target = "productColor",         source = "orderedProductColor")
    @Mapping(target = "quantity",             source = "quantity")
    @Mapping(target = "orderProductImageUrl", source = "representativeURL")
    OrderItemResponseDto toDto(OrderProduct orderProduct);

    @Mapping(target = "orderId",     source = "id")
    @Mapping(target = "orderNumber", source = "orderNumber")
    @Mapping(target = "orderName",   source = "orderName")
    @Mapping(target = "totalAmount", source = "totalAmount")
    @Mapping(target = "createdAt",   source = "createdAt")
    @Mapping(target = "status",      expression = "java(order.getStatus().getStatusName().name())")
    @Mapping(target = "items",       source = "orderProducts")
    OrderResponse toDto(Order order);
}
