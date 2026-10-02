package com.sema.ecommerce.service;

import com.sema.ecommerce.dto.response.OrderResponse;
import com.sema.ecommerce.enums.OrderStatus;

public interface OrderService {
    OrderResponse createOrder(Long userId);

    OrderResponse getOrderById(Long orderId);

    OrderResponse updateOrderStatus(
            Long orderId,
            OrderStatus status
    );
}
