package com.shop.theshop;

import com.shop.theshop.entities.Cart;
import com.shop.theshop.entities.TotalPrice;
import com.shop.theshop.entities.User;
import com.shop.theshop.repositories.CartRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class CartRepositoryTest {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    public void shouldSaveAndRetrieveCart() {
        // Given
        User user = new User();
        entityManager.persist(user);

        TotalPrice totalPrice = new TotalPrice(BigDecimal.TEN);

        Cart cart = new Cart(user, totalPrice);

        // When
        cartRepository.save(cart);
        entityManager.flush();
        entityManager.clear();

        // Then
        Cart retrievedCart = cartRepository.findById(cart.getId()).orElse(null);
        assertThat(retrievedCart).isNotNull();
        assertThat(retrievedCart.getUser()).isEqualTo(user);
        assertThat(retrievedCart.getTotalPrice()).isEqualTo(totalPrice);
    }
}
