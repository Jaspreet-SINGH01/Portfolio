package com.shop.theshop;

import com.shop.theshop.entities.Product;
import com.shop.theshop.entities.Review;
import com.shop.theshop.entities.User;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class ReviewTest {

    @Test
    public void shouldCreateReview() {
        // Given
        Product product = new Product();
        User user = new User();
        int rating = 4;
        String comment = "Great product!";

        // When
        Review review = new Review(product, user, rating, comment);

        // Then
        assertThat(review).isNotNull();
        assertThat(review.getProduct()).isEqualTo(product);
        assertThat(review.getUser()).isEqualTo(user);
        assertThat(review.getRating()).isEqualTo(rating);
        assertThat(review.getComment()).isEqualTo(comment);
    }

    @Test
    public void shouldUpdateReview() {
        // Given
        Product initialProduct = new Product();
        User initialUser = new User();
        int initialRating = 4;
        String initialComment = "Great product!";

        Review review = new Review(initialProduct, initialUser, initialRating, initialComment);

        Product updatedProduct = new Product();
        User updatedUser = new User();
        int updatedRating = 5;
        String updatedComment = "Excellent product!";

        // When
        review.setProduct(updatedProduct);
        review.setUser(updatedUser);
        review.setRating(updatedRating);
        review.setComment(updatedComment);

        // Then
        assertThat(review.getProduct()).isEqualTo(updatedProduct);
        assertThat(review.getUser()).isEqualTo(updatedUser);
        assertThat(review.getRating()).isEqualTo(updatedRating);
        assertThat(review.getComment()).isEqualTo(updatedComment);
    }

    @Test
    public void shouldCheckEqualsAndHashCode() {
        // Given
        Product product1 = new Product();
        User user1 = new User();
        int rating1 = 4;
        String comment1 = "Great product!";

        Review review1 = new Review(product1, user1, rating1, comment1);

        Product product2 = new Product();
        User user2 = new User();
        int rating2 = 4;
        String comment2 = "Great product!";

        Review review2 = new Review(product2, user2, rating2, comment2);

        // Then
        assertThat(review1.equals(review2)).isTrue();
        assertThat(review1.hashCode()).isEqualTo(review2.hashCode());
    }
}

