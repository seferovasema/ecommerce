package com.sema.ecommerce.mapper;

import com.sema.ecommerce.dto.request.ProductRequest;
import com.sema.ecommerce.dto.response.ProductResponse;
import com.sema.ecommerce.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "category", ignore = true)
    Product toEntity(ProductRequest request);

    @Mapping(target = "categoryId", source = "category.id")
    ProductResponse toResponse(Product product);

    @Mapping(target = "category", ignore = true)
    void updateProduct(ProductRequest request, @MappingTarget Product product);
}
