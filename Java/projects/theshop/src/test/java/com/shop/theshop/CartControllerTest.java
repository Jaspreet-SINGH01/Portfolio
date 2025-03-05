package com.shop.theshop;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shop.theshop.controller.CartController;
import com.shop.theshop.entities.Cart;
import com.shop.theshop.entities.TotalPrice;
import com.shop.theshop.services.CartService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartController.class)
public class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Mock
    private CartService cartService;

    @Test
    public void shouldGetAllCarts() throws Exception {
        Cart cart1 = new Cart();
        Cart cart2 = new Cart();
        List<Cart> cartList = Arrays.asList(cart1, cart2);

        when(cartService.getAllCarts()).thenReturn(cartList);

        mockMvc.perform(get("/carts"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    public void shouldGetCartById() throws Exception {
        Long cartId = 1L;
        Cart cart = new Cart();
        cart.setId(cartId);

        when(cartService.getCartById(cartId)).thenReturn(cart);

        mockMvc.perform(get("/carts/{id}", cartId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(cartId));
    }

    @Test
    public void shouldCreateCart() throws Exception {
        Cart cart = new Cart();
        when(cartService.createCart(cart)).thenReturn(cart);

        mockMvc.perform(post("/carts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cart)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    public void shouldUpdateCart() throws Exception {
        Long cartId = 1L;
        Cart existingCart = new Cart();
        existingCart.setId(cartId);

        Cart updatedCart = new Cart();
        updatedCart.setTotalPrice(new TotalPrice(BigDecimal.TEN));

        when(cartService.updateCart(eq(cartId), any(Cart.class))).thenReturn(existingCart);

        mockMvc.perform(put("/carts/{id}", cartId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedCart)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(cartId));
    }

    @Test
    public void shouldDeleteCart() throws Exception {
        Long cartId = 1L;

        mockMvc.perform(delete("/carts/{id}", cartId))
                .andExpect(status().isOk());

        verify(cartService, times(1)).deleteCart(cartId);
    }
}