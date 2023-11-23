package com.shop.theshop.repositories;

import com.shop.theshop.entities.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class MyCommandLineRunner implements CommandLineRunner {

    private final CartRepository cartRepository;

    @Autowired
    public MyCommandLineRunner(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Création d'un objet Cart
        Cart cart = new Cart();
        cart.setUserId(1L);
        cart.setTotalPrice(100.00);

        // Sauvegarde dans le repository
        cartRepository.save(cart);
    }
}
