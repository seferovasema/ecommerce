package com.sema.ecommerce.mapper;

import com.sema.ecommerce.dto.request.CategoryRequest;
import com.sema.ecommerce.dto.response.CategoryResponse;
import com.sema.ecommerce.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    Category toEntity(CategoryRequest request);
    CategoryResponse toResponse(Category category);
    void updateCategory(CategoryRequest request, @MappingTarget Category category);
}
