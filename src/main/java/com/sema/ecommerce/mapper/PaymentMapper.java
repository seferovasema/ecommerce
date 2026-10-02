package com.sema.ecommerce.mapper;

import com.sema.ecommerce.dto.response.PaymentResponse;
import com.sema.ecommerce.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(target = "paymentId", source = "id")
    @Mapping(target = "orderId", source = "order.id")
    PaymentResponse toResponse(Payment payment);
}