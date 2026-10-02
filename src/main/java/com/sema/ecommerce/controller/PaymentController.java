package com.sema.ecommerce.controller;

import com.sema.ecommerce.dto.request.PaymentRequest;
import com.sema.ecommerce.dto.response.PaymentResponse;
import com.sema.ecommerce.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(
            @Valid @RequestBody PaymentRequest request) {

        return ResponseEntity.ok(
                paymentService.createPayment(request)
        );
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPaymentById(
            @PathVariable Long paymentId) {

        return ResponseEntity.ok(
                paymentService.getPaymentById(paymentId)
        );
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponse> getPaymentByOrderId(
            @PathVariable Long orderId) {

        return ResponseEntity.ok(
                paymentService.getPaymentByOrderId(orderId)
        );
    }

    @PostMapping("/{paymentId}/process")
    public ResponseEntity<PaymentResponse> processPayment(
            @PathVariable Long paymentId) {

        return ResponseEntity.ok(
                paymentService.processPayment(paymentId)
        );
    }

    @PostMapping("/{paymentId}/fail")
    public ResponseEntity<PaymentResponse> failPayment(
            @PathVariable Long paymentId) {

        return ResponseEntity.ok(
                paymentService.failPayment(paymentId)
        );
    }
}