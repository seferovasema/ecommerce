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

import com.sema.ecommerce.dto.request.CartItemRequest;
import com.sema.ecommerce.dto.request.CartItemUpdateRequest;
import com.sema.ecommerce.dto.response.CartResponse;
import com.sema.ecommerce.service.CartService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = CartController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = jakarta.servlet.Filter.class))
@AutoConfigureMockMvc(addFilters = false)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CartService cartService;

    @Test
    void getCart_shouldReturnOk() throws Exception {
        when(cartService.getCart(1L)).thenReturn(mock(CartResponse.class));

        mockMvc.perform(get("/api/carts/1"))
                .andExpect(status().isOk());

        verify(cartService).getCart(1L);
    }

    @Test
    void addItem_shouldReturnOk() throws Exception {
        when(cartService.addItem(eq(1L), any(CartItemRequest.class)))
                .thenReturn(mock(CartResponse.class));

        mockMvc.perform(post("/api/carts/1/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":1,\"quantity\":2}"))
                .andExpect(status().isOk());

        verify(cartService).addItem(eq(1L), any(CartItemRequest.class));
    }

    @Test
    void addItem_shouldReturnBadRequestWhenBodyInvalid() throws Exception {
        mockMvc.perform(post("/api/carts/1/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verify(cartService, never()).addItem(any(), any());
    }

    @Test
    void updateItem_shouldReturnOk() throws Exception {
        when(cartService.updateItem(eq(1L), eq(5L), any(CartItemUpdateRequest.class)))
                .thenReturn(mock(CartResponse.class));

        mockMvc.perform(put("/api/carts/1/items/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":3}"))
                .andExpect(status().isOk());

        verify(cartService).updateItem(eq(1L), eq(5L), any(CartItemUpdateRequest.class));
    }

    @Test
    void removeItem_shouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/carts/1/items/5"))
                .andExpect(status().isNoContent());

        verify(cartService).removeItem(1L, 5L);
    }

    @Test
    void clearCart_shouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/carts/1/items"))
                .andExpect(status().isNoContent());

        verify(cartService).clearCart(1L);
    }
}