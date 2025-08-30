package org.example.sansam.order.mapper;


import org.example.sansam.order.domain.Order;
import org.example.sansam.order.domain.OrderProduct;
import org.example.sansam.order.dto.OrderWithProductsResponse;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderSummaryMapper {

    @Mappings({
            @Mapping(target = "orderNumber", source = "orderNumber"),
            @Mapping(target = "totalAmount", source = "totalAmount"),
            @Mapping(target = "createdAt", source = "createdAt"),
            @Mapping(target = "orderStatus", source = "status.statusName"),
            @Mapping(target = "items", source = "orderProducts")
    })
    OrderWithProductsResponse toOrderDto(Order order);

    @Mappings({
            @Mapping(target = "orderProductId", source = "id"),
            @Mapping(target = "productId",      source = "product.id"),
            @Mapping(target = "productName",    source = "product.productName"),
            @Mapping(target = "productPrice",   source = "orderedProductPrice"),
            @Mapping(target = "productSize",    source = "orderedProductSize"),
            @Mapping(target = "productColor",   source = "orderedProductColor"),
            @Mapping(target = "quantity",       source = "quantity"),
            @Mapping(target = "orderProductImageUrl", source = "representativeURL"),
            @Mapping(target = "orderProductStatus",   source = "status.statusName")
    })
    OrderWithProductsResponse.ProductSummary toProductSummary(OrderProduct op);

}
