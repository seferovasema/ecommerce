package com.sema.ecommerce.mapper;

import com.sema.ecommerce.dto.response.OrderItemResponse;
import com.sema.ecommerce.dto.response.OrderResponse;
import com.sema.ecommerce.entity.Order;
import com.sema.ecommerce.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "orderId", source = "id")
    @Mapping(target = "userId", source = "user.id")
    OrderResponse toResponse(Order order);

    @Mapping(target = "orderItemId", source = "id")
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(
            target = "subtotal",
            expression = "java(orderItem.getPrice().multiply(java.math.BigDecimal.valueOf(orderItem.getQuantity())))"
    )
    OrderItemResponse toItemResponse(OrderItem orderItem);
}