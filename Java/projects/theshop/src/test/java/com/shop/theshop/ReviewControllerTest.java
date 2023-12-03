package com.shop.theshop;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shop.theshop.entities.Review;
import com.shop.theshop.services.ReviewService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ReviewService reviewService;

    @Test
    public void shouldGetAllReviews() throws Exception {
        Review review1 = new Review();
        Review review2 = new Review();

        reviewService.createReview(review1);
        reviewService.createReview(review2);

        MvcResult result = mockMvc.perform(get("/reviews"))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
    }

    @Test
    public void shouldGetReviewById() throws Exception {
        Review review = new Review();
        reviewService.createReview(review);

        MvcResult result = mockMvc.perform(get("/reviews/{id}", review.getId()))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
    }

    @Test
    public void shouldCreateReview() throws Exception {
        Review review = new Review();

        MvcResult result = mockMvc.perform(post("/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(review)))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
    }

    @Test
    public void shouldUpdateReview() throws Exception {
        // Given
        Review review = new Review();
        reviewService.createReview(review);

        // When
        review.setRating(5);
        MvcResult result = mockMvc.perform(put("/reviews/{id}", review.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(review)))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        String content = result.getResponse().getContentAsString();
    }

    @Test
    public void shouldDeleteReview() throws Exception {
        // Given
        Review review = new Review();
        reviewService.createReview(review);

        // When
        mockMvc.perform(delete("/reviews/{id}", review.getId()))
                .andExpect(status().isOk());
    }
}

