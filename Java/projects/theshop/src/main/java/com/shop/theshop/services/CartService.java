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
        // Ajoutez une logique de validation ou de traitement si nécessaire
        return cartRepository.save(cart);
    }

    public Cart updateCart(Long id, Cart updatedCart) {
        // Vérifiez si le panier existe avant de le mettre à jour
        Optional<Cart> optionalCart = cartRepository.findById(id);
        if (optionalCart.isPresent()) {
            Cart existingCart = optionalCart.get();
            // Mettez à jour les champs nécessaires
            existingCart.setTotalPrice(updatedCart.getTotalPrice());
            // Ajoutez d'autres mises à jour si nécessaire
            return cartRepository.save(existingCart);
        } else {
            // Le panier n'existe pas, vous pouvez choisir de lever une exception ou de créer un nouveau panier
            return null;
        }
    }

    public void deleteCart(Long id) {
        cartRepository.deleteById(id);
    }
}
