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
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CartItemMapper cartItemMapper;

    @Override
    public CartResponse getCart(Long userId) {

        Cart cart = getOrCreateCart(userId);

        return buildCartResponse(cart);
    }

    @Override
    public CartResponse addItem(
            Long userId,
            CartItemRequest request) {

        Cart cart = getOrCreateCart(userId);

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: "
                                + request.getProductId()
                ));

        CartItem existingItem = cart.getItems()
                .stream()
                .filter(item -> item.getProduct().getId()
                        .equals(product.getId()))
                .findFirst()
                .orElse(null);

        int newQuantity =
                (existingItem != null
                        ? existingItem.getQuantity()
                        : 0)
                        + request.getQuantity();

        if (newQuantity > product.getStock()) {
            throw new InsufficientStockException(
                    "Not enough stock for product: "
                            + product.getName()
            );
        }

        if (existingItem != null) {

            existingItem.setQuantity(newQuantity);

        } else {

            CartItem cartItem = cartItemMapper.toEntity(request);

            cartItem.setProduct(product);

            cart.addItem(cartItem);
        }

        cartRepository.flush();

        return buildCartResponse(cart);
    }

    @Override
    public CartResponse updateItem(
            Long userId,
            Long cartItemId,
            CartItemUpdateRequest request) {

        Cart cart = findCart(userId);

        CartItem cartItem = findItem(cart, cartItemId);

        Product product = cartItem.getProduct();

        if (request.getQuantity() > product.getStock()) {
            throw new InsufficientStockException(
                    "Not enough stock for product: "
                            + product.getName()
            );
        }

        cartItem.setQuantity(request.getQuantity());

        return buildCartResponse(cart);
    }

    @Override
    public void removeItem(
            Long userId,
            Long cartItemId) {

        Cart cart = findCart(userId);

        CartItem cartItem = findItem(cart, cartItemId);

        cart.removeItem(cartItem);
    }

    @Override
    public void clearCart(Long userId) {

        Cart cart = findCart(userId);

        cart.getItems().clear();
    }

    private Cart findCart(Long userId) {

        return cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cart not found for user id: " + userId
                ));
    }

    private CartItem findItem(
            Cart cart,
            Long cartItemId) {

        return cart.getItems()
                .stream()
                .filter(item -> item.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cart item not found with id: " + cartItemId
                ));
    }

    private Cart getOrCreateCart(Long userId) {

        return cartRepository.findByUserId(userId)
                .orElseGet(() -> {

                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "User not found for id: " + userId
                            ));

                    Cart cart = new Cart();
                    cart.setUser(user);

                    return cartRepository.save(cart);
                });
    }

    private CartResponse buildCartResponse(Cart cart) {

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

    private BigDecimal calculateTotalAmount(Cart cart) {

        return cart.getItems()
                .stream()
                .map(item -> item.getProduct()
                        .getPrice()
                        .multiply(
                                BigDecimal.valueOf(
                                        item.getQuantity()
                                )
                        )
                )
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );
    }
}