package com.sema.ecommerce.service.impl;

import com.sema.ecommerce.dto.request.CategoryRequest;
import com.sema.ecommerce.dto.response.CategoryResponse;
import com.sema.ecommerce.entity.Category;
import com.sema.ecommerce.exception.ResourceAlreadyExistsException;
import com.sema.ecommerce.exception.ResourceNotFoundException;
import com.sema.ecommerce.mapper.CategoryMapper;
import com.sema.ecommerce.repository.CategoryRepository;
import com.sema.ecommerce.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public CategoryResponse createCategory(CategoryRequest request) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new ResourceAlreadyExistsException(
                    "Category already exists: " + request.getName());
        }
        Category category=categoryMapper.toEntity(request);
        categoryRepository.save(category);
        return categoryMapper.toResponse(category);
    }

    @Override
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {

        Category category=categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (!category.getName().equals(request.getName())
                && categoryRepository.existsByName(request.getName())) {

            throw new ResourceAlreadyExistsException(
                    "Category already exists: " + request.getName());
        }
         categoryMapper.updateCategory(request,category);
        Category updatedCategory = categoryRepository.save(category);

        return categoryMapper.toResponse(updatedCategory);
    }

    @Override
    public CategoryResponse getCategoryById(Long id) {
        Category category=categoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        return categoryMapper.toResponse(category);
    }

    @Override
    public void deleteCategory(Long id) {
        Category category=categoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Category not found"));
          categoryRepository.delete(category);
    }
}
