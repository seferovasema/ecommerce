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

import com.sema.ecommerce.dto.request.ProductRequest;
import com.sema.ecommerce.dto.response.PageResponse;
import com.sema.ecommerce.dto.response.ProductResponse;
import com.sema.ecommerce.enums.SortDirection;
import com.sema.ecommerce.enums.SortField;
import com.sema.ecommerce.service.ProductService;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = ProductController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = jakarta.servlet.Filter.class))
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerTest {

    private static final String VALID_BODY =
            "{\"name\":\"Laptop\",\"description\":\"Gaming laptop\","
                    + "\"price\":999.99,\"stock\":10,\"categoryId\":1}";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @Test
    void createProduct_shouldReturnCreated() throws Exception {
        when(productService.createProduct(any(ProductRequest.class)))
                .thenReturn(mock(ProductResponse.class));

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isCreated());

        verify(productService).createProduct(any(ProductRequest.class));
    }

    @Test
    void createProduct_shouldReturnBadRequestWhenBodyInvalid() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verify(productService, never()).createProduct(any());
    }

    @Test
    void getProductById_shouldReturnOk() throws Exception {
        when(productService.getProductById(1L)).thenReturn(mock(ProductResponse.class));

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk());
    }

    @Test
    void updateProduct_shouldReturnOk() throws Exception {
        when(productService.updateProduct(eq(1L), any(ProductRequest.class)))
                .thenReturn(mock(ProductResponse.class));

        mockMvc.perform(put("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isOk());

        verify(productService).updateProduct(eq(1L), any(ProductRequest.class));
    }

    @Test
    void deleteProduct_shouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isNoContent());

        verify(productService).deleteProduct(1L);
    }

    @Test
    @SuppressWarnings("unchecked")
    void getAllProducts_shouldUseDefaultParams() throws Exception {
        PageResponse<ProductResponse> page = mock(PageResponse.class);

        when(productService.getAllProducts(
                anyInt(), anyInt(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(page);

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk());

        verify(productService).getAllProducts(
                eq(0), eq(10),
                isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
                eq(SortField.ID), eq(SortDirection.ASC));
    }

    @Test
    @SuppressWarnings("unchecked")
    void getAllProducts_shouldPassFiltersToService() throws Exception {
        PageResponse<ProductResponse> page = mock(PageResponse.class);

        when(productService.getAllProducts(
                anyInt(), anyInt(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(page);

        mockMvc.perform(get("/api/products")
                        .param("page", "2")
                        .param("size", "5")
                        .param("name", "lap")
                        .param("categoryId", "3")
                        .param("minPrice", "10.50")
                        .param("maxPrice", "99.90")
                        .param("minStock", "1")
                        .param("maxStock", "50")
                        .param("sortBy", "ID")
                        .param("direction", "DESC"))
                .andExpect(status().isOk());

        verify(productService).getAllProducts(
                2, 5, "lap", 3L,
                new BigDecimal("10.50"), new BigDecimal("99.90"),
                1, 50,
                SortField.ID, SortDirection.DESC);
    }
}