package com.sema.ecommerce.service.impl;

import com.sema.ecommerce.dto.request.CartItemRequest;
import com.sema.ecommerce.dto.request.CartItemUpdateRequest;
import com.sema.ecommerce.dto.response.CartItemResponse;
import com.sema.ecommerce.dto.response.CartResponse;
import com.sema.ecommerce.entity.Cart;
import com.sema.ecommerce.entity.CartItem;
import com.sema.ecommerce.entity.Product;
import com.sema.ecommerce.entity.User;
import com.sema.ecommerce.exception.InsufficientStockException;
import com.sema.ecommerce.exception.ResourceNotFoundException;
import com.sema.ecommerce.mapper.CartItemMapper;
import com.sema.ecommerce.repository.CartRepository;
import com.sema.ecommerce.repository.ProductRepository;
import com.sema.ecommerce.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CartItemMapper cartItemMapper;

    @InjectMocks
    private CartServiceImpl cartService;


    @Test
    void getCart_shouldReturnCart_whenCartExists() {
        User user = user(1L);
        Cart cart = cart(10L, user);
        Product product = product(1L, "Keyboard", "20.00", 10);
        CartItem item = item(100L, product, 2);

        cart.addItem(item);

        when(cartRepository.findByUserId(1L))
                .thenReturn(Optional.of(cart));
        when(cartItemMapper.toResponse(item))
                .thenReturn(mock(CartItemResponse.class));

        CartResponse result = cartService.getCart(1L);

        assertNotNull(result);
        assertEquals(new BigDecimal("40.00"), result.getTotalAmount());
        verify(cartRepository).findByUserId(1L);
    }

    @Test
    void getCart_shouldCreateCart_whenCartDoesNotExist() {
        User user = user(1L);
        Cart savedCart = cart(10L, user);

        when(cartRepository.findByUserId(1L))
                .thenReturn(Optional.empty());
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));
        when(cartRepository.save(any(Cart.class)))
                .thenReturn(savedCart);

        CartResponse result = cartService.getCart(1L);

        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.getTotalAmount());
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void getCart_shouldThrowException_whenUserDoesNotExist() {
        when(cartRepository.findByUserId(1L))
                .thenReturn(Optional.empty());
        when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> cartService.getCart(1L)
        );

        verify(cartRepository, never()).save(any(Cart.class));
    }


    @Test
    void addItem_shouldAddNewItem_whenStockIsAvailable() {
        User user = user(1L);
        Cart cart = cart(10L, user);
        Product product = product(5L, "Mouse", "15.00", 10);

        CartItemRequest request = mock(CartItemRequest.class);
        when(request.getProductId()).thenReturn(5L);
        when(request.getQuantity()).thenReturn(2);

        CartItem newItem = item(null, product, 2);

        when(cartRepository.findByUserId(1L))
                .thenReturn(Optional.of(cart));
        when(productRepository.findById(5L))
                .thenReturn(Optional.of(product));
        when(cartItemMapper.toEntity(request)).thenReturn(newItem);
        when(cartItemMapper.toResponse(newItem))
                .thenReturn(mock(CartItemResponse.class));

        CartResponse result = cartService.addItem(1L, request);

        assertNotNull(result);
        assertEquals(1, cart.getItems().size());
        assertEquals(new BigDecimal("30.00"), result.getTotalAmount());
        verify(cartRepository).flush();
    }

    @Test
    void addItem_shouldThrowException_whenProductDoesNotExist() {
        User user = user(1L);
        Cart cart = cart(10L, user);

        CartItemRequest request = mock(CartItemRequest.class);
        when(request.getProductId()).thenReturn(99L);

        when(cartRepository.findByUserId(1L))
                .thenReturn(Optional.of(cart));
        when(productRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> cartService.addItem(1L, request)
        );

        verify(cartRepository, never()).flush();
    }

    @Test
    void addItem_shouldThrowException_whenQuantityExceedsStock() {
        User user = user(1L);
        Cart cart = cart(10L, user);
        Product product = product(5L, "Mouse", "15.00", 3);

        CartItemRequest request = mock(CartItemRequest.class);
        when(request.getProductId()).thenReturn(5L);
        when(request.getQuantity()).thenReturn(4);

        when(cartRepository.findByUserId(1L))
                .thenReturn(Optional.of(cart));
        when(productRepository.findById(5L))
                .thenReturn(Optional.of(product));

        assertThrows(
                InsufficientStockException.class,
                () -> cartService.addItem(1L, request)
        );

        verify(cartRepository, never()).flush();
    }

    @Test
    void addItem_shouldIncreaseQuantity_whenItemAlreadyExists() {
        User user = user(1L);
        Cart cart = cart(10L, user);
        Product product = product(5L, "Mouse", "15.00", 10);
        CartItem existingItem = item(100L, product, 2);
        cart.addItem(existingItem);

        CartItemRequest request = mock(CartItemRequest.class);
        when(request.getProductId()).thenReturn(5L);
        when(request.getQuantity()).thenReturn(3);

        when(cartRepository.findByUserId(1L))
                .thenReturn(Optional.of(cart));
        when(productRepository.findById(5L))
                .thenReturn(Optional.of(product));
        when(cartItemMapper.toResponse(existingItem))
                .thenReturn(mock(CartItemResponse.class));

        cartService.addItem(1L, request);

        assertEquals(5, existingItem.getQuantity());
        verify(cartRepository).flush();
        verify(cartItemMapper, never()).toEntity(request);
    }



    @Test
    void updateItem_shouldUpdateQuantity_whenStockIsAvailable() {
        User user = user(1L);
        Cart cart = cart(10L, user);
        Product product = product(5L, "Mouse", "15.00", 10);
        CartItem item = item(100L, product, 2);
        cart.addItem(item);

        CartItemUpdateRequest request =
                mock(CartItemUpdateRequest.class);
        when(request.getQuantity()).thenReturn(4);

        when(cartRepository.findByUserId(1L))
                .thenReturn(Optional.of(cart));
        when(cartItemMapper.toResponse(item))
                .thenReturn(mock(CartItemResponse.class));

        CartResponse result =
                cartService.updateItem(1L, 100L, request);

        assertNotNull(result);
        assertEquals(4, item.getQuantity());
        assertEquals(new BigDecimal("60.00"), result.getTotalAmount());
    }

    @Test
    void updateItem_shouldThrowException_whenCartDoesNotExist() {
        CartItemUpdateRequest request =
                mock(CartItemUpdateRequest.class);

        when(cartRepository.findByUserId(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> cartService.updateItem(1L, 100L, request)
        );
    }

    @Test
    void updateItem_shouldThrowException_whenItemDoesNotExist() {
        Cart cart = cart(10L, user(1L));
        CartItemUpdateRequest request =
                mock(CartItemUpdateRequest.class);

        when(cartRepository.findByUserId(1L))
                .thenReturn(Optional.of(cart));

        assertThrows(
                ResourceNotFoundException.class,
                () -> cartService.updateItem(1L, 100L, request)
        );
    }

    @Test
    void updateItem_shouldThrowException_whenQuantityExceedsStock() {
        User user = user(1L);
        Cart cart = cart(10L, user);
        Product product = product(5L, "Mouse", "15.00", 3);
        CartItem item = item(100L, product, 2);
        cart.addItem(item);

        CartItemUpdateRequest request =
                mock(CartItemUpdateRequest.class);
        when(request.getQuantity()).thenReturn(4);

        when(cartRepository.findByUserId(1L))
                .thenReturn(Optional.of(cart));

        assertThrows(
                InsufficientStockException.class,
                () -> cartService.updateItem(1L, 100L, request)
        );

        assertEquals(2, item.getQuantity());
    }



    @Test
    void removeItem_shouldRemoveItem_whenItemExists() {
        User user = user(1L);
        Cart cart = cart(10L, user);
        CartItem item = item(
                100L, product(5L, "Mouse", "15.00", 10), 2);
        cart.addItem(item);

        when(cartRepository.findByUserId(1L))
                .thenReturn(Optional.of(cart));

        cartService.removeItem(1L, 100L);

        assertTrue(cart.getItems().isEmpty());
    }

    @Test
    void removeItem_shouldThrowException_whenCartDoesNotExist() {
        when(cartRepository.findByUserId(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> cartService.removeItem(1L, 100L)
        );
    }

    @Test
    void removeItem_shouldThrowException_whenItemDoesNotExist() {
        Cart cart = cart(10L, user(1L));

        when(cartRepository.findByUserId(1L))
                .thenReturn(Optional.of(cart));

        assertThrows(
                ResourceNotFoundException.class,
                () -> cartService.removeItem(1L, 100L)
        );
    }



    @Test
    void clearCart_shouldRemoveAllItems_whenCartExists() {
        User user = user(1L);
        Cart cart = cart(10L, user);
        cart.addItem(item(
                100L, product(5L, "Mouse", "15.00", 10), 2));
        cart.addItem(item(
                101L, product(6L, "Keyboard", "30.00", 10), 1));

        when(cartRepository.findByUserId(1L))
                .thenReturn(Optional.of(cart));

        cartService.clearCart(1L);

        assertTrue(cart.getItems().isEmpty());
    }

    @Test
    void clearCart_shouldThrowException_whenCartDoesNotExist() {
        when(cartRepository.findByUserId(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> cartService.clearCart(1L)
        );
    }


    private User user(Long id) {
        User user = new User();
        user.setId(id);
        return user;
    }

    private Cart cart(Long id, User user) {
        Cart cart = new Cart();
        cart.setId(id);
        cart.setUser(user);
        return cart;
    }

    private Product product(
            Long id, String name, String price, int stock) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setPrice(new BigDecimal(price));
        product.setStock(stock);
        return product;
    }

    private CartItem item(
            Long id, Product product, int quantity) {
        CartItem item = new CartItem();
        item.setId(id);
        item.setProduct(product);
        item.setQuantity(quantity);
        return item;
    }
}
