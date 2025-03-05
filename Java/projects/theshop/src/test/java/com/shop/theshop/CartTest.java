package com.shop.theshop;

import com.shop.theshop.entities.Cart;
import com.shop.theshop.entities.TotalPrice;
import com.shop.theshop.entities.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class CartTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void shouldCreateCart() {
        User user = new User();
        TotalPrice totalPrice = new TotalPrice();

        Cart cart = new Cart(user, totalPrice);

        assertThat(cart).isNotNull();
        assertThat(cart.getUser()).isEqualTo(user);
        assertThat(cart.getTotalPrice()).isEqualTo(totalPrice);
    }

    @Test
    public void shouldSetAndGetUser() {
        User user = new User();
        Cart cart = new Cart();

        cart.setUser(user);

        assertThat(cart.getUser()).isEqualTo(user);
    }

    @Test
    public void shouldSetAndGetTotalPrice() {
        TotalPrice totalPrice = new TotalPrice();
        Cart cart = new Cart();

        cart.setTotalPrice(totalPrice);

        assertThat(cart.getTotalPrice()).isEqualTo(totalPrice);
    }

    @Test
    public void shouldSetAndGetId() {
        Long id = 1L;
        Cart cart = new Cart();

        cart.setId(id);

        assertThat(cart.getId()).isEqualTo(id);
    }
}