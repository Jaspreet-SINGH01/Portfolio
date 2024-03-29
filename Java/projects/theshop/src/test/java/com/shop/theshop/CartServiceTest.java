package com.shop.theshop;

import com.shop.theshop.entities.Cart;
import com.shop.theshop.entities.TotalPrice;
import com.shop.theshop.repositories.CartRepository;
import com.shop.theshop.services.CartService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
public class CartServiceTest {

    @Autowired
    private CartService cartService;

    @Mock
    private CartRepository cartRepository;

    @Test
    public void shouldGetAllCarts() {
        Cart cart1 = new Cart();
        Cart cart2 = new Cart();
        List<Cart> cartList = Arrays.asList(cart1, cart2);

        when(cartRepository.findAll()).thenReturn(cartList);

        List<Cart> result = cartService.getAllCarts();

        assertThat(result).hasSize(2);
    }

    @Test
    public void shouldGetCartById() {
        Long cartId = 1L;
        Cart cart = new Cart();
        cart.setId(cartId);

        when(cartRepository.findById(cartId)).thenReturn(Optional.of(cart));

        Cart result = cartService.getCartById(cartId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(cartId);
    }

    @Test
    public void shouldCreateCart() {
        Cart cart = new Cart();
        when(cartRepository.save(cart)).thenReturn(cart);

        Cart result = cartService.createCart(cart);

        assertThat(result).isNotNull();
    }

    @Test
    public void shouldUpdateCart() {
        Long cartId = 1L;
        Cart existingCart = new Cart();
        existingCart.setId(cartId);

        Cart updatedCart = new Cart();
        updatedCart.setTotalPrice(new TotalPrice(BigDecimal.TEN));

        when(cartRepository.findById(cartId)).thenReturn(Optional.of(existingCart));
        when(cartRepository.save(existingCart)).thenReturn(existingCart);

        Cart result = cartService.updateCart(cartId, updatedCart);

        assertThat(result).isNotNull();
        assertThat(result.getTotalPrice()).isEqualTo(updatedCart.getTotalPrice());
    }

    @Test
    public void shouldDeleteCart() {
        Long cartId = 1L;

        cartService.deleteCart(cartId);

        verify(cartRepository, times(1)).deleteById(cartId);
    }
}