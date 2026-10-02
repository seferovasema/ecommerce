package com.sema.ecommerce.service;

import com.sema.ecommerce.dto.request.CartItemRequest;
import com.sema.ecommerce.dto.response.CartResponse;

public interface CartService {

    CartResponse getCart(Long userId);

    CartResponse addItem(Long userId, CartItemRequest request);

    CartResponse updateItem(Long userId, Long cartItemId, CartItemRequest request);

    void removeItem(Long userId, Long cartItemId);

    void clearCart(Long userId);
}