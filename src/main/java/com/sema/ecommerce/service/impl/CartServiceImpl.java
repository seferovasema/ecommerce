package com.sema.ecommerce.service.impl;

import com.sema.ecommerce.dto.request.CartItemRequest;
import com.sema.ecommerce.dto.response.CartItemResponse;
import com.sema.ecommerce.dto.response.CartResponse;
import com.sema.ecommerce.entity.Cart;
import com.sema.ecommerce.entity.CartItem;
import com.sema.ecommerce.entity.Product;
import com.sema.ecommerce.entity.User;
import com.sema.ecommerce.exception.ResourceNotFoundException;
import com.sema.ecommerce.mapper.CartItemMapper;
import com.sema.ecommerce.repository.CartItemRepository;
import com.sema.ecommerce.repository.CartRepository;
import com.sema.ecommerce.repository.ProductRepository;
import com.sema.ecommerce.repository.UserRepository;
import com.sema.ecommerce.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CartItemMapper cartItemMapper;

    @Override
    public CartResponse getCart(Long userId) {

        Cart cart = getOrCreateCart(userId);

        BigDecimal totalAmount = calculateTotalAmount(cart);

        List<CartItemResponse> items = cart.getItems()
                .stream()
                .map(cartItemMapper::toResponse)
                .toList();

        return CartResponse.builder()
                .cartId(cart.getId())
                .userId(cart.getUser().getId())
                .items(items)
                .totalAmount(totalAmount)
                .build();
    }

    @Override
    public CartResponse addItem(
            Long userId,
            CartItemRequest request) {

        Cart cart = getOrCreateCart(userId);

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + request.getProductId()
                ));

        if (product.getStock() < request.getQuantity()) {
            throw new IllegalArgumentException(
                    "Not enough stock for product: " + product.getName()
            );
        }

        CartItem cartItem = cart.getItems()
                .stream()
                .filter(item -> item.getProduct().getId()
                        .equals(product.getId()))
                .findFirst()
                .orElse(null);

        if (cartItem != null) {

            int newQuantity =
                    cartItem.getQuantity() + request.getQuantity();

            if (newQuantity > product.getStock()) {
                throw new IllegalArgumentException(
                        "Not enough stock for product: " + product.getName()
                );
            }

            cartItem.setQuantity(newQuantity);

        } else {

            cartItem = cartItemMapper.toEntity(request);

            cartItem.setCart(cart);
            cartItem.setProduct(product);

            cart.getItems().add(cartItem);
        }

        cartItemRepository.save(cartItem);

        return getCart(userId);
    }

    @Override
    public CartResponse updateItem(
            Long userId,
            Long cartItemId,
            CartItemRequest request) {

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cart not found for user id: " + userId
                ));

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cart item not found with id: " + cartItemId
                ));

        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new ResourceNotFoundException(
                    "Cart item does not belong to this cart"
            );
        }

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + request.getProductId()
                ));

        if (product.getStock() < request.getQuantity()) {
            throw new IllegalArgumentException(
                    "Not enough stock for product: " + product.getName()
            );
        }

        cartItem.setProduct(product);
        cartItem.setQuantity(request.getQuantity());

        cartItemRepository.save(cartItem);

        return getCart(userId);
    }

    @Override
    public void removeItem(
            Long userId,
            Long cartItemId) {

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cart not found for user id: " + userId
                ));

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cart item not found with id: " + cartItemId
                ));

        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new ResourceNotFoundException(
                    "Cart item does not belong to this cart"
            );
        }

        cartItemRepository.delete(cartItem);
    }

    @Override
    public void clearCart(Long userId) {

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cart not found for user id: " + userId
                ));

        cart.getItems().clear();

        cartRepository.save(cart);
    }

    private BigDecimal calculateTotalAmount(Cart cart) {

        return cart.getItems()
                .stream()
                .map(item -> item.getProduct()
                        .getPrice()
                        .multiply(
                                BigDecimal.valueOf(item.getQuantity())
                        )
                )
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Cart getOrCreateCart(Long userId) {

        return cartRepository.findByUserId(userId)
                .orElseGet(() -> {

                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "User not found with id: " + userId
                            ));

                    Cart cart = new Cart();
                    cart.setUser(user);

                    return cartRepository.save(cart);
                });
    }
}