package com.sema.ecommerce.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.sema.ecommerce.dto.request.PaymentRequest;
import com.sema.ecommerce.dto.response.PaymentResponse;
import com.sema.ecommerce.service.PaymentService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = PaymentController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = jakarta.servlet.Filter.class))
@AutoConfigureMockMvc(addFilters = false)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentService paymentService;

    @Test
    void createPayment_shouldReturnOk() throws Exception {
        when(paymentService.createPayment(any(PaymentRequest.class)))
                .thenReturn(mock(PaymentResponse.class));

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orderId\":1,\"paymentMethod\":\"CARD\"}"))
                .andExpect(status().isOk());

        verify(paymentService).createPayment(any(PaymentRequest.class));
    }

    @Test
    void createPayment_shouldReturnBadRequestWhenBodyInvalid() throws Exception {
        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verify(paymentService, never()).createPayment(any());
    }

    @Test
    void getPaymentById_shouldReturnOk() throws Exception {
        when(paymentService.getPaymentById(1L)).thenReturn(mock(PaymentResponse.class));

        mockMvc.perform(get("/api/payments/1"))
                .andExpect(status().isOk());
    }

    @Test
    void getPaymentByOrderId_shouldReturnOk() throws Exception {
        when(paymentService.getPaymentByOrderId(7L)).thenReturn(mock(PaymentResponse.class));

        mockMvc.perform(get("/api/payments/order/7"))
                .andExpect(status().isOk());
    }

    @Test
    void processPayment_shouldReturnOk() throws Exception {
        when(paymentService.processPayment(1L)).thenReturn(mock(PaymentResponse.class));

        mockMvc.perform(post("/api/payments/1/process"))
                .andExpect(status().isOk());

        verify(paymentService).processPayment(1L);
    }

    @Test
    void failPayment_shouldReturnOk() throws Exception {
        when(paymentService.failPayment(1L)).thenReturn(mock(PaymentResponse.class));

        mockMvc.perform(post("/api/payments/1/fail"))
                .andExpect(status().isOk());

        verify(paymentService).failPayment(1L);
    }
}