// Package dans src/test/java
package com.shop.theshop;

import com.shop.theshop.entities.Product;
import com.shop.theshop.repositories.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Test
    public void shouldSaveAndRetrieveProduct() {
        // Given
        Product product = new Product("Test Product", "Description", 29.99);

        // When
        productRepository.save(product);

        // Then
        Product retrievedProduct = productRepository.findById(product.getId()).orElse(null);
        assertThat(retrievedProduct).isNotNull();
        assertThat(retrievedProduct.getName()).isEqualTo("Test Product");
        assertThat(retrievedProduct.getDescription()).isEqualTo("Description");
        assertThat(retrievedProduct.getPrice()).isEqualTo(29.99);
    }

    @Test
    public void shouldDeleteProduct() {
        // Given
        Product product = new Product("Test Product", "Description", 29.99);
        productRepository.save(product);

        // When
        productRepository.deleteById(product.getId());

        // Then
        assertThat(productRepository.findById(product.getId())).isEmpty();
    }
}
