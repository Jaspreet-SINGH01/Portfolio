package com.shop.theshop;

import com.shop.theshop.entities.Category;
import com.shop.theshop.repositories.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    public void shouldSaveAndRetrieveCategory() {
        // Given
        Category category = new Category();
        category.setName("Electronics");

        // When
        categoryRepository.save(category);

        // Then
        Category retrievedCategory = categoryRepository.findById(category.getId()).orElse(null);
        assertThat(retrievedCategory).isNotNull();
        assertThat(retrievedCategory.getName()).isEqualTo("Electronics");
    }

    @Test
    public void shouldFindCategoryByName() {
        // Given
        Category category1 = new Category();
        category1.setName("Electronics");

        Category category2 = new Category();
        category2.setName("Clothing");

        categoryRepository.saveAll(List.of(category1, category2));

        // When
        Optional<Category> foundCategory1 = categoryRepository.findByName("Electronics");
        Optional<Category> foundCategory2 = categoryRepository.findByName("Clothing");
        Optional<Category> notFoundCategory = categoryRepository.findByName("NonExistentCategory");

        // Then
        assertThat(foundCategory1).isPresent();
        assertThat(foundCategory1.get().getName()).isEqualTo("Electronics");

        assertThat(foundCategory2).isPresent();
        assertThat(foundCategory2.get().getName()).isEqualTo("Clothing");

        assertThat(notFoundCategory).isNotPresent();
    }
}
