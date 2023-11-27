package com.shop.theshop;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shop.theshop.entities.Product;
import com.shop.theshop.services.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductService productService;

    @Test
    public void shouldGetAllProducts() throws Exception {
        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    public void shouldGetProductById() throws Exception {
        long productId = 1L;

        mockMvc.perform(get("/products/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(productId));
    }

    @Test
    public void shouldCreateProduct() throws Exception {
        Product newProduct = new Product("New Product", "Description", 19.99);

        ResultActions result = mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newProduct)))
                .andExpect(status().isCreated());


        Product createdProduct = objectMapper.readValue(result.andReturn().getResponse().getContentAsString(), Product.class);
    }

    @Test
    public void shouldUpdateProduct() throws Exception {
        long productId = 1L;

        Product updatedProduct = new Product("Updated Product", "Updated Description", 29.99);

        mockMvc.perform(put("/products/{id}", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedProduct)))
                .andExpect(status().isOk());

    }

    @Test
    public void shouldDeleteProduct() throws Exception {
        long productId = 1L;

        mockMvc.perform(delete("/products/{id}", productId))
                .andExpect(status().isOk());

    }
}
