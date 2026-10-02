package com.sema.ecommerce.service;

import com.sema.ecommerce.dto.request.ProductRequest;
import com.sema.ecommerce.dto.response.PageResponse;
import com.sema.ecommerce.dto.response.ProductResponse;
import com.sema.ecommerce.enums.SortDirection;
import com.sema.ecommerce.enums.SortField;

import java.math.BigDecimal;

public interface ProductService {
    ProductResponse createProduct(ProductRequest request);

    ProductResponse getProductById(Long id);

    ProductResponse updateProduct(Long id, ProductRequest request);

    void deleteProduct(Long id);

    PageResponse<ProductResponse> getAllProducts(
            int page,
            int size,
            String name,
            Long categoryId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Integer minStock,
            Integer maxStock,
            SortField sortBy,
            SortDirection direction
    );
}
