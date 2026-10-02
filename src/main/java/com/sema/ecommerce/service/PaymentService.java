package com.sema.ecommerce.service;

import com.sema.ecommerce.dto.request.PaymentRequest;
import com.sema.ecommerce.dto.response.PaymentResponse;

public interface PaymentService {

    PaymentResponse createPayment(PaymentRequest request);

    PaymentResponse getPaymentById(Long paymentId);

    PaymentResponse getPaymentByOrderId(Long orderId);

    PaymentResponse processPayment(Long paymentId);

    PaymentResponse failPayment(Long paymentId);
}