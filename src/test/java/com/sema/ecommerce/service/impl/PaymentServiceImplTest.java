package com.sema.ecommerce.service.impl;

import com.sema.ecommerce.dto.request.PaymentRequest;
import com.sema.ecommerce.dto.response.PaymentResponse;
import com.sema.ecommerce.entity.Order;
import com.sema.ecommerce.entity.Payment;
import com.sema.ecommerce.enums.OrderStatus;
import com.sema.ecommerce.enums.PaymentStatus;
import com.sema.ecommerce.exception.ResourceAlreadyExistsException;
import com.sema.ecommerce.exception.ResourceNotFoundException;
import com.sema.ecommerce.mapper.PaymentMapper;
import com.sema.ecommerce.repository.OrderRepository;
import com.sema.ecommerce.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PaymentMapper paymentMapper;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Test
    void createPayment_shouldCreatePaymentSuccessfully() {
        Long orderId = 1L;

        PaymentRequest request = mock(PaymentRequest.class);
        when(request.getOrderId()).thenReturn(orderId);
        when(request.getPaymentMethod()).thenReturn(null);

        Order order = mock(Order.class);
        when(order.getId()).thenReturn(orderId);
        when(order.getTotalAmount()).thenReturn(new BigDecimal("200.00"));

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));
        when(paymentRepository.existsByOrderId(orderId))
                .thenReturn(false);
        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PaymentResponse response = mock(PaymentResponse.class);
        when(paymentMapper.toResponse(any(Payment.class)))
                .thenReturn(response);

        PaymentResponse result = paymentService.createPayment(request);

        assertSame(response, result);
        verify(paymentRepository).save(any(Payment.class));
        verify(paymentMapper).toResponse(any(Payment.class));
    }

    @Test
    void createPayment_shouldThrowExceptionWhenOrderNotFound() {
        Long orderId = 1L;

        PaymentRequest request = mock(PaymentRequest.class);
        when(request.getOrderId()).thenReturn(orderId);

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> paymentService.createPayment(request)
        );

        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void createPayment_shouldThrowExceptionWhenPaymentAlreadyExists() {
        Long orderId = 1L;

        PaymentRequest request = mock(PaymentRequest.class);
        when(request.getOrderId()).thenReturn(orderId);

        Order order = mock(Order.class);
        when(order.getId()).thenReturn(orderId);

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));
        when(paymentRepository.existsByOrderId(orderId))
                .thenReturn(true);

        assertThrows(
                ResourceAlreadyExistsException.class,
                () -> paymentService.createPayment(request)
        );

        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void getPaymentById_shouldReturnPaymentSuccessfully() {
        Long paymentId = 1L;

        Payment payment = mock(Payment.class);
        PaymentResponse response = mock(PaymentResponse.class);

        when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.of(payment));
        when(paymentMapper.toResponse(payment)).thenReturn(response);

        PaymentResponse result = paymentService.getPaymentById(paymentId);

        assertSame(response, result);
        verify(paymentMapper).toResponse(payment);
    }

    @Test
    void getPaymentById_shouldThrowExceptionWhenNotFound() {
        Long paymentId = 1L;

        when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> paymentService.getPaymentById(paymentId)
        );
    }

    @Test
    void getPaymentByOrderId_shouldReturnPaymentSuccessfully() {
        Long orderId = 1L;

        Payment payment = mock(Payment.class);
        PaymentResponse response = mock(PaymentResponse.class);

        when(paymentRepository.findByOrderId(orderId))
                .thenReturn(Optional.of(payment));
        when(paymentMapper.toResponse(payment)).thenReturn(response);

        PaymentResponse result =
                paymentService.getPaymentByOrderId(orderId);

        assertSame(response, result);
    }

    @Test
    void getPaymentByOrderId_shouldThrowExceptionWhenNotFound() {
        Long orderId = 1L;

        when(paymentRepository.findByOrderId(orderId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> paymentService.getPaymentByOrderId(orderId)
        );
    }

    @Test
    void processPayment_shouldMarkPaymentSuccessfulAndConfirmOrder() {
        Long paymentId = 1L;

        Payment payment = mock(Payment.class);
        Order order = mock(Order.class);
        PaymentResponse response = mock(PaymentResponse.class);

        when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.of(payment));
        when(payment.getStatus()).thenReturn(PaymentStatus.PENDING);
        when(payment.getOrder()).thenReturn(order);
        when(paymentMapper.toResponse(payment)).thenReturn(response);

        PaymentResponse result = paymentService.processPayment(paymentId);

        verify(payment).setStatus(PaymentStatus.SUCCESS);
        verify(order).setStatus(OrderStatus.CONFIRMED);
        assertSame(response, result);
    }

    @Test
    void processPayment_shouldThrowExceptionWhenAlreadyProcessed() {
        Long paymentId = 1L;

        Payment payment = mock(Payment.class);
        when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.of(payment));
        when(payment.getStatus()).thenReturn(PaymentStatus.SUCCESS);

        assertThrows(
                IllegalArgumentException.class,
                () -> paymentService.processPayment(paymentId)
        );

        verify(paymentMapper, never()).toResponse(any(Payment.class));
    }

    @Test
    void processPayment_shouldThrowExceptionWhenPaymentNotFound() {
        Long paymentId = 1L;

        when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> paymentService.processPayment(paymentId)
        );
    }

    @Test
    void failPayment_shouldMarkPaymentFailedAndCancelOrder() {
        Long paymentId = 1L;

        Payment payment = mock(Payment.class);
        Order order = mock(Order.class);
        PaymentResponse response = mock(PaymentResponse.class);

        when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.of(payment));
        when(payment.getStatus()).thenReturn(PaymentStatus.PENDING);
        when(payment.getOrder()).thenReturn(order);
        when(paymentMapper.toResponse(payment)).thenReturn(response);

        PaymentResponse result = paymentService.failPayment(paymentId);

        verify(payment).setStatus(PaymentStatus.FAILED);
        verify(order).setStatus(OrderStatus.CANCELLED);
        assertSame(response, result);
    }

    @Test
    void failPayment_shouldThrowExceptionWhenAlreadyProcessed() {
        Long paymentId = 1L;

        Payment payment = mock(Payment.class);
        when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.of(payment));
        when(payment.getStatus()).thenReturn(PaymentStatus.FAILED);

        assertThrows(
                IllegalArgumentException.class,
                () -> paymentService.failPayment(paymentId)
        );

        verify(paymentMapper, never()).toResponse(any(Payment.class));
    }

    @Test
    void failPayment_shouldThrowExceptionWhenPaymentNotFound() {
        Long paymentId = 1L;

        when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> paymentService.failPayment(paymentId)
        );
    }
}
