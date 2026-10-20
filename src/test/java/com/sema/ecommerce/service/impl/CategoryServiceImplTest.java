package com.sema.ecommerce.service.impl;

import com.sema.ecommerce.dto.request.CategoryRequest;
import com.sema.ecommerce.dto.response.CategoryResponse;
import com.sema.ecommerce.entity.Category;
import com.sema.ecommerce.exception.ResourceAlreadyExistsException;
import com.sema.ecommerce.exception.ResourceNotFoundException;
import com.sema.ecommerce.mapper.CategoryMapper;
import com.sema.ecommerce.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    void createCategory_shouldCreateSuccessfully() {
        CategoryRequest request = mock(CategoryRequest.class);
        Category category = mock(Category.class);
        CategoryResponse response = mock(CategoryResponse.class);

        when(request.getName()).thenReturn("Electronics");
        when(categoryRepository.existsByName("Electronics"))
                .thenReturn(false);
        when(categoryMapper.toEntity(request)).thenReturn(category);
        when(categoryRepository.save(category)).thenReturn(category);
        when(categoryMapper.toResponse(category)).thenReturn(response);

        CategoryResponse result = categoryService.createCategory(request);

        assertSame(response, result);
        verify(categoryRepository).save(category);
        verify(categoryMapper).toResponse(category);
    }

    @Test
    void createCategory_shouldThrowExceptionWhenNameAlreadyExists() {
        CategoryRequest request = mock(CategoryRequest.class);
        when(request.getName()).thenReturn("Electronics");

        when(categoryRepository.existsByName("Electronics"))
                .thenReturn(true);

        assertThrows(
                ResourceAlreadyExistsException.class,
                () -> categoryService.createCategory(request)
        );

        verify(categoryRepository, never()).save(any(Category.class));
        verify(categoryMapper, never()).toEntity(any(CategoryRequest.class));
    }

    @Test
    void updateCategory_shouldUpdateSuccessfully() {
        Long id = 1L;

        CategoryRequest request = mock(CategoryRequest.class);
        when(request.getName()).thenReturn("Books");

        Category category = mock(Category.class);
        when(category.getName()).thenReturn("Electronics");

        CategoryResponse response = mock(CategoryResponse.class);

        when(categoryRepository.findById(id))
                .thenReturn(Optional.of(category));
        when(categoryRepository.existsByName("Books"))
                .thenReturn(false);
        when(categoryRepository.save(category)).thenReturn(category);
        when(categoryMapper.toResponse(category)).thenReturn(response);

        CategoryResponse result = categoryService.updateCategory(id, request);

        assertSame(response, result);
        verify(categoryMapper).updateCategory(request, category);
        verify(categoryRepository).save(category);
    }

    @Test
    void updateCategory_shouldThrowExceptionWhenCategoryNotFound() {
        Long id = 1L;
        CategoryRequest request = mock(CategoryRequest.class);

        when(categoryRepository.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> categoryService.updateCategory(id, request)
        );

        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void updateCategory_shouldThrowExceptionWhenNewNameAlreadyExists() {
        Long id = 1L;

        CategoryRequest request = mock(CategoryRequest.class);
        when(request.getName()).thenReturn("Books");

        Category category = mock(Category.class);
        when(category.getName()).thenReturn("Electronics");

        when(categoryRepository.findById(id))
                .thenReturn(Optional.of(category));
        when(categoryRepository.existsByName("Books"))
                .thenReturn(true);

        assertThrows(
                ResourceAlreadyExistsException.class,
                () -> categoryService.updateCategory(id, request)
        );

        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void getCategoryById_shouldReturnCategorySuccessfully() {
        Long id = 1L;

        Category category = mock(Category.class);
        CategoryResponse response = mock(CategoryResponse.class);

        when(categoryRepository.findById(id))
                .thenReturn(Optional.of(category));
        when(categoryMapper.toResponse(category)).thenReturn(response);

        CategoryResponse result = categoryService.getCategoryById(id);

        assertSame(response, result);
    }

    @Test
    void getCategoryById_shouldThrowExceptionWhenNotFound() {
        Long id = 1L;

        when(categoryRepository.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> categoryService.getCategoryById(id)
        );
    }

    @Test
    void deleteCategory_shouldDeleteSuccessfully() {
        Long id = 1L;
        Category category = mock(Category.class);

        when(categoryRepository.findById(id))
                .thenReturn(Optional.of(category));

        categoryService.deleteCategory(id);

        verify(categoryRepository).delete(category);
    }

    @Test
    void deleteCategory_shouldThrowExceptionWhenNotFound() {
        Long id = 1L;

        when(categoryRepository.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> categoryService.deleteCategory(id)
        );

        verify(categoryRepository, never()).delete(any(Category.class));
    }
}
