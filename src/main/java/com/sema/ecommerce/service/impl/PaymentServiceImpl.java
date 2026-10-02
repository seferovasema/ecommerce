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
import com.sema.ecommerce.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PaymentMapper paymentMapper;

    @Override
    public PaymentResponse createPayment(
            PaymentRequest request) {

        Order order = orderRepository.findById(
                request.getOrderId()
        ).orElseThrow(() -> new ResourceNotFoundException(
                "Order not found with id: "
                        + request.getOrderId()
        ));

        if (paymentRepository.existsByOrderId(order.getId())) {
            throw new ResourceAlreadyExistsException(
                    "Payment already exists for order id: "
                            + order.getId()
            );
        }

        Payment payment = new Payment();

        payment.setOrder(order);
        payment.setAmount(order.getTotalAmount());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setPaymentMethod(request.getPaymentMethod());

        Payment savedPayment =
                paymentRepository.save(payment);

        return paymentMapper.toResponse(savedPayment);
    }

    @Override
    public PaymentResponse getPaymentById(Long paymentId) {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment not found with id: "
                                + paymentId
                ));

        return paymentMapper.toResponse(payment);
    }

    @Override
    public PaymentResponse getPaymentByOrderId(Long orderId) {

        Payment payment = paymentRepository
                .findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment not found for order id: "
                                + orderId
                ));

        return paymentMapper.toResponse(payment);
    }

    @Override
    public PaymentResponse processPayment(Long paymentId) {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment not found with id: " + paymentId
                ));

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new IllegalArgumentException(
                    "Payment is already processed"
            );
        }

        payment.setStatus(PaymentStatus.SUCCESS);


        Order order = payment.getOrder();
        order.setStatus(OrderStatus.CONFIRMED);

        return paymentMapper.toResponse(payment);
    }

    @Override
    public PaymentResponse failPayment(Long paymentId) {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment not found with id: " + paymentId
                ));

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new IllegalArgumentException(
                    "Payment is already processed"
            );
        }

        payment.setStatus(PaymentStatus.FAILED);

        Order order = payment.getOrder();
        order.setStatus(OrderStatus.CANCELLED);

        return paymentMapper.toResponse(payment);
    }
}