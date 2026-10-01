package com.sema.ecommerce.service;

import com.sema.ecommerce.dto.request.CategoryRequest;
import com.sema.ecommerce.dto.response.CategoryResponse;

public interface CategoryService {
    CategoryResponse createCategory(CategoryRequest request);
    CategoryResponse updateCategory(Long id, CategoryRequest request);
    CategoryResponse getCategoryById(Long id);
    void deleteCategory(Long id);
}
