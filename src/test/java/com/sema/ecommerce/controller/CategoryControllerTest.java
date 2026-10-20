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

import com.sema.ecommerce.dto.request.CategoryRequest;
import com.sema.ecommerce.dto.response.CategoryResponse;
import com.sema.ecommerce.exception.ResourceNotFoundException;
import com.sema.ecommerce.service.CategoryService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = CategoryController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = jakarta.servlet.Filter.class))
@AutoConfigureMockMvc(addFilters = false)
class CategoryControllerTest {

    private static final String VALID_BODY =
            "{\"name\":\"Electronics\",\"description\":\"Electronic devices\"}";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryService categoryService;

    @Test
    void createCategory_shouldReturnCreated() throws Exception {
        when(categoryService.createCategory(any(CategoryRequest.class)))
                .thenReturn(mock(CategoryResponse.class));

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isCreated());

        verify(categoryService).createCategory(any(CategoryRequest.class));
    }

    @Test
    void createCategory_shouldReturnBadRequestWhenBodyInvalid() throws Exception {
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verify(categoryService, never()).createCategory(any());
    }

    @Test
    void getCategoryById_shouldReturnOk() throws Exception {
        when(categoryService.getCategoryById(1L)).thenReturn(mock(CategoryResponse.class));

        mockMvc.perform(get("/api/categories/1"))
                .andExpect(status().isOk());
    }

    @Test
    void getCategoryById_shouldReturnNotFound() throws Exception {
        when(categoryService.getCategoryById(99L))
                .thenThrow(new ResourceNotFoundException("Category not found with id: 99"));

        mockMvc.perform(get("/api/categories/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateCategory_shouldReturnOk() throws Exception {
        when(categoryService.updateCategory(eq(1L), any(CategoryRequest.class)))
                .thenReturn(mock(CategoryResponse.class));

        mockMvc.perform(put("/api/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isOk());

        verify(categoryService).updateCategory(eq(1L), any(CategoryRequest.class));
    }

    @Test
    void deleteCategory_shouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/categories/1"))
                .andExpect(status().isNoContent());

        verify(categoryService).deleteCategory(1L);
    }
}