package com.shop.theshop;

import com.shop.theshop.entities.Category;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class CategoryTest {

    @Test
    public void shouldCreateCategory() {
        // Given
        Long categoryId = 1L;
        String categoryName = "Electronics";

        // When
        Category category = new Category();
        category.setId(categoryId);
        category.setName(categoryName);

        // Then
        assertThat(category).isNotNull();
        assertThat(category.getId()).isEqualTo(categoryId);
        assertThat(category.getName()).isEqualTo(categoryName);
    }

    @Test
    public void shouldUpdateCategory() {
        // Given
        Long categoryId = 1L;
        String initialCategoryName = "Electronics";
        String updatedCategoryName = "Clothing";

        Category category = new Category();
        category.setId(categoryId);
        category.setName(initialCategoryName);

        // When
        category.setName(updatedCategoryName);

        // Then
        assertThat(category.getName()).isEqualTo(updatedCategoryName);
    }
}
