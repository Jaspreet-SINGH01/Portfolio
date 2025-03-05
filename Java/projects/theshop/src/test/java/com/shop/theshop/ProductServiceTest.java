package com.shop.theshop;

import com.shop.theshop.entities.Product;
import com.shop.theshop.repositories.ProductRepository;
import com.shop.theshop.services.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest
public class ProductServiceTest {

    @Autowired
    private ProductService productService;

    @MockBean
    private ProductRepository productRepository;

    @Test
    public void shouldGetProductById() {
        // Given
        long productId = 1L;
        Product mockProduct = new Product("Test Product", "Description", 29.99);
        when(productRepository.findById(productId)).thenReturn(Optional.of(mockProduct));

        // When
        Product retrievedProduct = productService.getProductById(productId);

        // Then
        assertThat(retrievedProduct).isNotNull();
        assertThat(retrievedProduct.getName()).isEqualTo("Test Product");
        assertThat(retrievedProduct.getDescription()).isEqualTo("Description");
        assertThat(retrievedProduct.getPrice()).isEqualTo(29.99);
    }

    @Test
    public void shouldCreateProduct() {
        Product newProduct = new Product("New Product", "New Description", 39.99);
        when(productRepository.save(newProduct)).thenReturn(newProduct);

        Product createdProduct = productService.createProduct(newProduct);

        assertThat(createdProduct).isNotNull();
        assertThat(createdProduct.getName()).isEqualTo("New Product");
        assertThat(createdProduct.getDescription()).isEqualTo("New Description");
        assertThat(createdProduct.getPrice()).isEqualTo(39.99);
    }

    @Test
    public void shouldUpdateProduct() {
        long productId = 1L;
        Product existingProduct = new Product("Existing Product", "Description", 29.99);
        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));

        Product updatedProduct = new Product("Updated Product", "Updated Description", 39.99);

        Product updatedProductResult = productService.updateProduct(productId, updatedProduct);

        assertThat(updatedProductResult).isNotNull();
        assertThat(updatedProductResult.getName()).isEqualTo("Updated Product");
        assertThat(updatedProductResult.getDescription()).isEqualTo("Updated Description");
        assertThat(updatedProductResult.getPrice()).isEqualTo(39.99);
    }

    @Test
    public void shouldDeleteProduct() {
        long productId = 1L;
        Product existingProduct = new Product("Existing Product", "Description", 29.99);
        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));

        productService.deleteProduct(productId);

    }
}