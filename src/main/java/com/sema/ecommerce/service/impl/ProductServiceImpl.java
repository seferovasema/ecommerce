package com.sema.ecommerce.service.impl;

import com.sema.ecommerce.dto.request.ProductRequest;
import com.sema.ecommerce.dto.response.PageResponse;
import com.sema.ecommerce.dto.response.ProductResponse;
import com.sema.ecommerce.entity.Category;
import com.sema.ecommerce.entity.Product;
import com.sema.ecommerce.enums.SortDirection;
import com.sema.ecommerce.enums.SortField;
import com.sema.ecommerce.exception.ResourceNotFoundException;
import com.sema.ecommerce.mapper.ProductMapper;
import com.sema.ecommerce.repository.CategoryRepository;
import com.sema.ecommerce.repository.ProductRepository;
import com.sema.ecommerce.service.ProductService;
import com.sema.ecommerce.specification.ProductSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;


@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    @Override
    public ProductResponse createProduct(ProductRequest request) {

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found with id: " + request.getCategoryId()));

        Product product = productMapper.toEntity(request);

        product.setCategory(category);

        Product savedProduct = productRepository.save(product);

        return productMapper.toResponse(savedProduct);
    }

    @Override
    public ProductResponse getProductById(Long id) {
        Product product=productRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                "Product not found with id: " + id));
        return productMapper.toResponse(product);
    }

    @Override
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + id));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found with id: " + request.getCategoryId()));

        productMapper.updateProduct(request, product);

        product.setCategory(category);

        Product updatedProduct = productRepository.save(product);

        return productMapper.toResponse(updatedProduct);
    }

    @Override
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + id));

        productRepository.delete(product);
    }


    @Override
    public PageResponse<ProductResponse> getAllProducts(
            int page,
            int size,
            String name,
            Long categoryId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Integer minStock,
            Integer maxStock,
            SortField sortBy,
            SortDirection direction) {

        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new IllegalArgumentException(
                    "Minimum price cannot be greater than maximum price"
            );
        }

        if (minStock != null && maxStock != null && minStock > maxStock) {
            throw new IllegalArgumentException(
                    "Minimum stock cannot be greater than maximum stock"
            );
        }

        Specification<Product> specification = Specification.allOf();

        if (name != null && !name.isBlank()) {
            specification = specification.and(
                    ProductSpecification.hasName(name)
            );
        }

        if (categoryId != null) {
            specification = specification.and(
                    ProductSpecification.hasCategoryId(categoryId)
            );
        }

        if (minPrice != null) {
            specification = specification.and(
                    ProductSpecification.hasMinPrice(minPrice)
            );
        }

        if (maxPrice != null) {
            specification = specification.and(
                    ProductSpecification.hasMaxPrice(maxPrice)
            );
        }
        if (minStock != null) {
            specification = specification.and(
                    ProductSpecification.hasMinStock(minStock)
            );
        }

        if (maxStock != null) {
            specification = specification.and(
                    ProductSpecification.hasMaxStock(maxStock)
            );
        }

        String field = switch (sortBy) {
            case ID -> "id";
            case NAME -> "name";
            case PRICE -> "price";
            case STOCK -> "stock";
            case CREATED_AT -> "createdAt";
        };

        Sort sort = direction == SortDirection.DESC
                ? Sort.by(field).descending()
                : Sort.by(field).ascending();

        Page<Product> products = productRepository.findAll(
                specification,
                PageRequest.of(page, size, sort)
        );

        Page<ProductResponse> responses =
                products.map(productMapper::toResponse);

        return new PageResponse<>(
                responses.getContent(),
                responses.getNumber(),
                responses.getSize(),
                responses.getTotalElements(),
                responses.getTotalPages(),
                responses.isFirst(),
                responses.isLast()
        );
    }

}
