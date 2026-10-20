package com.sema.ecommerce.controller;

import com.sema.ecommerce.dto.response.OrderResponse;
import com.sema.ecommerce.enums.OrderStatus;
import com.sema.ecommerce.exception.ResourceNotFoundException;
import com.sema.ecommerce.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = OrderController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = jakarta.servlet.Filter.class))
@AutoConfigureMockMvc(addFilters = false)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @Test
    void createOrder_shouldReturnOk() throws Exception {
        when(orderService.createOrder(1L)).thenReturn(mock(OrderResponse.class));

        mockMvc.perform(post("/api/orders/1"))
                .andExpect(status().isOk());

        verify(orderService).createOrder(1L);
    }

    @Test
    void getOrderById_shouldReturnOk() throws Exception {
        when(orderService.getOrderById(1L)).thenReturn(mock(OrderResponse.class));

        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isOk());
    }

    @Test
    void getOrderById_shouldReturnNotFound() throws Exception {
        when(orderService.getOrderById(99L))
                .thenThrow(new ResourceNotFoundException("Order not found with id: 99"));

        mockMvc.perform(get("/api/orders/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateOrderStatus_shouldReturnOk() throws Exception {
        when(orderService.updateOrderStatus(1L, OrderStatus.CONFIRMED))
                .thenReturn(mock(OrderResponse.class));

        mockMvc.perform(patch("/api/orders/1/status")
                        .param("status", "CONFIRMED"))
                .andExpect(status().isOk());

        verify(orderService).updateOrderStatus(1L, OrderStatus.CONFIRMED);
    }

    @Test
    void updateOrderStatus_shouldReturnBadRequestWhenStatusMissing() throws Exception {
        mockMvc.perform(patch("/api/orders/1/status"))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).updateOrderStatus(any(), any());
    }

    @Test
    void updateOrderStatus_shouldReturnBadRequestWhenStatusInvalid() throws Exception {
        mockMvc.perform(patch("/api/orders/1/status")
                        .param("status", "INVALID"))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).updateOrderStatus(any(), any());
    }
}