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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductServiceImpl productService;


    @Test
    void createProduct_shouldReturnResponse_whenCategoryExists() {
        ProductRequest request = mock(ProductRequest.class);
        when(request.getCategoryId()).thenReturn(1L);

        Category category = new Category();
        Product product = new Product();
        Product savedProduct = new Product();
        ProductResponse response = mock(ProductResponse.class);

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));
        when(productMapper.toEntity(request)).thenReturn(product);
        when(productRepository.save(product)).thenReturn(savedProduct);
        when(productMapper.toResponse(savedProduct)).thenReturn(response);

        ProductResponse result = productService.createProduct(request);

        assertSame(response, result);
        assertSame(category, product.getCategory());

        verify(categoryRepository).findById(1L);
        verify(productMapper).toEntity(request);
        verify(productRepository).save(product);
        verify(productMapper).toResponse(savedProduct);
    }

    @Test
    void createProduct_shouldThrowException_whenCategoryDoesNotExist() {
        ProductRequest request = mock(ProductRequest.class);
        when(request.getCategoryId()).thenReturn(99L);
        when(categoryRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> productService.createProduct(request)
        );

        verify(productRepository, never()).save(any());
    }


    @Test
    void getProductById_shouldReturnProduct_whenProductExists() {
        Long productId = 1L;
        Product product = new Product();
        ProductResponse response = mock(ProductResponse.class);

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(product));
        when(productMapper.toResponse(product)).thenReturn(response);

        ProductResponse result = productService.getProductById(productId);

        assertSame(response, result);
        verify(productRepository).findById(productId);
        verify(productMapper).toResponse(product);
    }

    @Test
    void getProductById_shouldThrowException_whenProductDoesNotExist() {
        Long productId = 99L;

        when(productRepository.findById(productId))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> productService.getProductById(productId)
        );

        assertNotNull(exception.getMessage());
        verify(productMapper, never()).toResponse(any());
    }



    @Test
    void updateProduct_shouldReturnResponse_whenProductAndCategoryExist() {
        Long productId = 1L;
        ProductRequest request = mock(ProductRequest.class);
        when(request.getCategoryId()).thenReturn(2L);

        Product product = new Product();
        Category category = new Category();
        ProductResponse response = mock(ProductResponse.class);

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(product));
        when(categoryRepository.findById(2L))
                .thenReturn(Optional.of(category));
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toResponse(product)).thenReturn(response);

        ProductResponse result =
                productService.updateProduct(productId, request);

        assertSame(response, result);
        assertSame(category, product.getCategory());

        verify(productMapper).updateProduct(request, product);
        verify(productRepository).save(product);
    }

    @Test
    void updateProduct_shouldThrowException_whenProductDoesNotExist() {
        Long productId = 99L;
        ProductRequest request = mock(ProductRequest.class);

        when(productRepository.findById(productId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> productService.updateProduct(productId, request)
        );

        verify(categoryRepository, never()).findById(any());
        verify(productRepository, never()).save(any());
    }

    @Test
    void updateProduct_shouldThrowException_whenCategoryDoesNotExist() {
        Long productId = 1L;
        ProductRequest request = mock(ProductRequest.class);
        when(request.getCategoryId()).thenReturn(99L);

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(new Product()));
        when(categoryRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> productService.updateProduct(productId, request)
        );

        verify(productRepository, never()).save(any());
    }


    @Test
    void deleteProduct_shouldDeleteProduct_whenProductExists() {
        Long productId = 1L;
        Product product = new Product();

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(product));

        assertDoesNotThrow(() -> productService.deleteProduct(productId));

        verify(productRepository).delete(product);
    }

    @Test
    void deleteProduct_shouldThrowException_whenProductDoesNotExist() {
        Long productId = 99L;

        when(productRepository.findById(productId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> productService.deleteProduct(productId)
        );

        verify(productRepository, never()).delete(any(Product.class));
    }



    @Test
    void getAllProducts_shouldReturnPageResponse_whenProductsExist() {
        Product product = new Product();
        ProductResponse response = mock(ProductResponse.class);

        Page<Product> productPage =
                new PageImpl<>(List.of(product));

        when(productRepository.findAll(
                any(Specification.class),
                any(Pageable.class)
        )).thenReturn(productPage);

        when(productMapper.toResponse(product)).thenReturn(response);

        PageResponse<ProductResponse> result =
                productService.getAllProducts(
                        0,
                        10,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        SortField.ID,
                        SortDirection.ASC
                );

        assertNotNull(result);
        verify(productRepository).findAll(
                any(Specification.class),
                any(Pageable.class)
        );
        verify(productMapper).toResponse(product);
    }

    @Test
    void getAllProducts_shouldThrowException_whenMinPriceGreaterThanMaxPrice() {
        BigDecimal minPrice = new BigDecimal("100");
        BigDecimal maxPrice = new BigDecimal("50");

        assertThrows(
                IllegalArgumentException.class,
                () -> productService.getAllProducts(
                        0, 10, null, null,
                        minPrice, maxPrice,
                        null, null,
                        SortField.ID, SortDirection.ASC
                )
        );

        verifyNoInteractions(productRepository);
    }

    @Test
    void getAllProducts_shouldThrowException_whenMinStockGreaterThanMaxStock() {
        assertThrows(
                IllegalArgumentException.class,
                () -> productService.getAllProducts(
                        0, 10, null, null,
                        null, null,
                        20, 10,
                        SortField.ID, SortDirection.ASC
                )
        );

        verifyNoInteractions(productRepository);
    }
}
