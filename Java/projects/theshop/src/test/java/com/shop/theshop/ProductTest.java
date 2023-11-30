package com.shop.theshop;

import com.shop.theshop.entities.Product;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class ProductTest {

    @Test
    public void shouldCreateProduct() {
        Long productId = 1L;
        String productName = "Test Product";
        String productDescription = "Description";
        double productPrice = 29.99;

        Product product = new Product();
        product.setId(productId);
        product.setName(productName);
        product.setDescription(productDescription);
        product.setPrice(productPrice);

        assertThat(product.getId()).isEqualTo(productId);
        assertThat(product.getName()).isEqualTo(productName);
        assertThat(product.getDescription()).isEqualTo(productDescription);
        assertThat(product.getPrice()).isEqualTo(productPrice);
    }
}
