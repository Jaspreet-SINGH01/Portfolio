package com.shop.theshop;

import com.shop.theshop.entities.Product;
import com.shop.theshop.entities.Review;
import com.shop.theshop.entities.User;
import com.shop.theshop.repositories.ReviewRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class ReviewRepositoryTest {

    @Autowired
    private ReviewRepository reviewRepository;

    @Test
    public void shouldSaveAndRetrieveReview() {
        // Given
        Product product = new Product();
        User user = new User();
        int rating = 4;
        String comment = "Great product!";

        Review review = new Review(product, user, rating, comment);

        // When
        reviewRepository.save(review);

        // Then
        Optional<Review> retrievedReview = reviewRepository.findById(review.getId());
        assertThat(retrievedReview).isPresent();
        assertThat(retrievedReview.get().getProduct()).isEqualTo(product);
        assertThat(retrievedReview.get().getUser()).isEqualTo(user);
        assertThat(retrievedReview.get().getRating()).isEqualTo(rating);
        assertThat(retrievedReview.get().getComment()).isEqualTo(comment);
    }

    @Test
    public void shouldFindReviewsByProduct() {
        // Given
        Product product = new Product();
        User user1 = new User();
        User user2 = new User();
        int rating1 = 4;
        int rating2 = 5;
        String comment1 = "Great product!";
        String comment2 = "Excellent product!";

        Review review1 = new Review(product, user1, rating1, comment1);
        Review review2 = new Review(product, user2, rating2, comment2);

        reviewRepository.saveAll(List.of(review1, review2));

        // When
        List<Review> reviews = reviewRepository.findByProduct(product);

        // Then
        assertThat(reviews).hasSize(2);
        assertThat(reviews).containsExactlyInAnyOrder(review1, review2);
    }
}
