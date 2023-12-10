package com.shop.theshop.services;

import com.shop.theshop.entities.Cart;
import com.shop.theshop.repositories.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    private final CartRepository cartRepository;

    @Autowired
    public CartService(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    public List<Cart> getAllCarts() {
        return cartRepository.findAll();
    }

    public Cart getCartById(Long id) {
        Optional<Cart> optionalCart = cartRepository.findById(id);
        return optionalCart.orElse(null);
    }

    public Cart createCart(Cart cart) {
        return cartRepository.save(cart);
    }

    public Cart updateCart(Long id, Cart updatedCart) {
        Optional<Cart> optionalCart = cartRepository.findById(id);
        if (optionalCart.isPresent()) {
            Cart existingCart = optionalCart.get();
            existingCart.setTotalPrice(updatedCart.getTotalPrice());
            return cartRepository.save(existingCart);
        } else {
            return null;
        }
    }

    public void deleteCart(Long id) {
        cartRepository.deleteById(id);
    }
}
