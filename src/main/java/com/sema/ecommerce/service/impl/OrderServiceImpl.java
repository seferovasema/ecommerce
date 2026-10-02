package com.sema.ecommerce.service.impl;

import com.sema.ecommerce.dto.response.OrderResponse;
import com.sema.ecommerce.entity.*;
import com.sema.ecommerce.enums.OrderStatus;
import com.sema.ecommerce.exception.InsufficientStockException;
import com.sema.ecommerce.exception.ResourceNotFoundException;
import com.sema.ecommerce.mapper.OrderMapper;
import com.sema.ecommerce.repository.CartRepository;
import com.sema.ecommerce.repository.OrderRepository;
import com.sema.ecommerce.repository.UserRepository;
import com.sema.ecommerce.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final OrderMapper orderMapper;
    @Override
    public OrderResponse createOrder(Long userId) {

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cart not found for user id: " + userId
                ));

        if (cart.getItems().isEmpty()) {
            throw new IllegalArgumentException(
                    "Cannot create order from an empty cart"
            );
        }

        for (CartItem cartItem : cart.getItems()) {

            Product product = cartItem.getProduct();

            if (cartItem.getQuantity() > product.getStock()) {
                throw new InsufficientStockException(
                        "Not enough stock for product: "
                                + product.getName()
                );
            }
        }

        Order order = new Order();

        order.setUser(cart.getUser());
        order.setStatus(OrderStatus.PENDING);

        for (CartItem cartItem : cart.getItems()) {

            Product product = cartItem.getProduct();

            OrderItem orderItem = new OrderItem();

            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(product.getPrice());

            order.addItem(orderItem);

            product.setStock(
                    product.getStock()
                            - cartItem.getQuantity()
            );
        }

        BigDecimal totalAmount = calculateTotalAmount(order);

        order.setTotalAmount(totalAmount);

        Order savedOrder = orderRepository.save(order);

        cart.getItems().clear();

        return orderMapper.toResponse(savedOrder);
    }

    @Override
    public OrderResponse getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found with id: " + orderId
                ));

        return orderMapper.toResponse(order);
    }

    @Override
    public OrderResponse updateOrderStatus(
            Long orderId,
            OrderStatus status) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found with id: " + orderId
                ));

        validateStatusTransition(
                order.getStatus(),
                status
        );

        order.setStatus(status);

        return orderMapper.toResponse(order);
    }

    private BigDecimal calculateTotalAmount(Order order) {

        return order.getItems()
                .stream()
                .map(item ->
                        item.getPrice()
                                .multiply(
                                        BigDecimal.valueOf(
                                                item.getQuantity()
                                        )
                                )
                )
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );
    }

    private void validateStatusTransition(
            OrderStatus currentStatus,
            OrderStatus newStatus) {

        boolean valid = switch (currentStatus) {

            case PENDING ->
                    newStatus == OrderStatus.CONFIRMED
                            || newStatus == OrderStatus.CANCELLED;

            case CONFIRMED ->
                    newStatus == OrderStatus.SHIPPED
                            || newStatus == OrderStatus.CANCELLED;

            case SHIPPED ->
                    newStatus == OrderStatus.DELIVERED;

            case DELIVERED, CANCELLED ->
                    false;
        };

        if (!valid) {
            throw new IllegalArgumentException(
                    "Invalid order status transition: "
                            + currentStatus
                            + " -> "
                            + newStatus
            );
        }
    }
}
