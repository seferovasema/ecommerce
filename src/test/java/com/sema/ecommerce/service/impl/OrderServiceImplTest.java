package com.sema.ecommerce.service.impl;

import com.sema.ecommerce.dto.response.OrderResponse;
import com.sema.ecommerce.entity.Cart;
import com.sema.ecommerce.entity.CartItem;
import com.sema.ecommerce.entity.Order;
import com.sema.ecommerce.entity.Product;
import com.sema.ecommerce.entity.User;
import com.sema.ecommerce.enums.OrderStatus;
import com.sema.ecommerce.exception.InsufficientStockException;
import com.sema.ecommerce.exception.ResourceNotFoundException;
import com.sema.ecommerce.mapper.OrderMapper;
import com.sema.ecommerce.repository.CartRepository;
import com.sema.ecommerce.repository.OrderRepository;
import com.sema.ecommerce.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderServiceImpl orderService;


    @Test
    void createOrder_shouldCreateOrderSuccessfully() {
        Long userId = 1L;

        Product product = mock(Product.class);
        when(product.getStock()).thenReturn(10);
        when(product.getPrice()).thenReturn(new BigDecimal("100.00"));

        CartItem cartItem = mock(CartItem.class);
        when(cartItem.getProduct()).thenReturn(product);
        when(cartItem.getQuantity()).thenReturn(2);

        User user = mock(User.class);

        Cart cart = mock(Cart.class);
        when(cart.getItems()).thenReturn(new ArrayList<>(List.of(cartItem)));
        when(cart.getUser()).thenReturn(user);

        when(cartRepository.findByUserId(userId))
                .thenReturn(Optional.of(cart));

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse response = mock(OrderResponse.class);
        when(orderMapper.toResponse(any(Order.class)))
                .thenReturn(response);

        OrderResponse result = orderService.createOrder(userId);

        assertSame(response, result);

        verify(orderRepository).save(any(Order.class));
        verify(product).setStock(8);
        verify(orderMapper).toResponse(any(Order.class));
    }

    @Test
    void createOrder_shouldThrowExceptionWhenCartNotFound() {
        Long userId = 1L;

        when(cartRepository.findByUserId(userId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> orderService.createOrder(userId)
        );

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void createOrder_shouldThrowExceptionWhenCartIsEmpty() {
        Long userId = 1L;

        Cart cart = mock(Cart.class);
        when(cart.getItems()).thenReturn(new ArrayList<>());

        when(cartRepository.findByUserId(userId))
                .thenReturn(Optional.of(cart));

        assertThrows(
                IllegalArgumentException.class,
                () -> orderService.createOrder(userId)
        );

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void createOrder_shouldThrowExceptionWhenStockIsInsufficient() {
        Long userId = 1L;

        Product product = mock(Product.class);
        when(product.getName()).thenReturn("Laptop");
        when(product.getStock()).thenReturn(1);

        CartItem cartItem = mock(CartItem.class);
        when(cartItem.getProduct()).thenReturn(product);
        when(cartItem.getQuantity()).thenReturn(2);

        Cart cart = mock(Cart.class);
        when(cart.getItems()).thenReturn(new ArrayList<>(List.of(cartItem)));

        when(cartRepository.findByUserId(userId))
                .thenReturn(Optional.of(cart));

        assertThrows(
                InsufficientStockException.class,
                () -> orderService.createOrder(userId)
        );

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void getOrderById_shouldReturnOrderSuccessfully() {
        Long orderId = 1L;

        Order order = mock(Order.class);
        OrderResponse response = mock(OrderResponse.class);

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));
        when(orderMapper.toResponse(order)).thenReturn(response);

        OrderResponse result = orderService.getOrderById(orderId);

        assertSame(response, result);
        verify(orderMapper).toResponse(order);
    }

    @Test
    void getOrderById_shouldThrowExceptionWhenOrderNotFound() {
        Long orderId = 1L;

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> orderService.getOrderById(orderId)
        );

        verify(orderMapper, never()).toResponse(any(Order.class));
    }

    @Test
    void updateOrderStatus_shouldUpdateStatusSuccessfully() {
        Long orderId = 1L;

        Order order = mock(Order.class);
        when(order.getStatus()).thenReturn(OrderStatus.PENDING);

        OrderResponse response = mock(OrderResponse.class);

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));
        when(orderMapper.toResponse(order)).thenReturn(response);

        OrderResponse result = orderService.updateOrderStatus(
                orderId,
                OrderStatus.CONFIRMED
        );

        verify(order).setStatus(OrderStatus.CONFIRMED);
        assertSame(response, result);
    }

    @Test
    void updateOrderStatus_shouldThrowExceptionForInvalidTransition() {
        Long orderId = 1L;

        Order order = mock(Order.class);
        when(order.getStatus()).thenReturn(OrderStatus.PENDING);

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        assertThrows(
                IllegalArgumentException.class,
                () -> orderService.updateOrderStatus(
                        orderId,
                        OrderStatus.DELIVERED
                )
        );

        verify(order, never()).setStatus(OrderStatus.DELIVERED);
        verify(orderMapper, never()).toResponse(any(Order.class));
    }

    @Test
    void updateOrderStatus_shouldThrowExceptionWhenOrderNotFound() {
        Long orderId = 1L;

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> orderService.updateOrderStatus(
                        orderId,
                        OrderStatus.CONFIRMED
                )
        );
    }
}
