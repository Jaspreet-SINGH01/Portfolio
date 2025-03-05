package com.shop.theshop;

import com.shop.theshop.entities.Category;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class CategoryTest {

    @Test
    public void shouldCreateCategory() {
        Long categoryId = 1L;
        String categoryName = "Electronics";

        Category category = new Category();
        category.setId(categoryId);
        category.setName(categoryName);

        assertThat(category).isNotNull();
        assertThat(category.getId()).isEqualTo(categoryId);
        assertThat(category.getName()).isEqualTo(categoryName);
    }

    @Test
    public void shouldUpdateCategory() {
        Long categoryId = 1L;
        String initialCategoryName = "Electronics";
        String updatedCategoryName = "Clothing";

        Category category = new Category();
        category.setId(categoryId);
        category.setName(initialCategoryName);

        category.setName(updatedCategoryName);

        assertThat(category.getName()).isEqualTo(updatedCategoryName);
    }
}