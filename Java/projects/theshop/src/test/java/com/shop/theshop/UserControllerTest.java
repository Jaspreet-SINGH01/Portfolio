package com.shop.theshop;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shop.theshop.entities.User;
import com.shop.theshop.services.UserService;
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
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    @Test
    public void shouldGetAllUsers() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    public void shouldGetUserById() throws Exception {
        // Assuming there is a user with id 1 in your test data
        long userId = 1L;

        mockMvc.perform(get("/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(userId));
    }

    @Test
    public void shouldCreateUser() throws Exception {
        User newUser = new User("newuser", "newpassword", "New User");

        ResultActions result = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isCreated());

        // You can extract information from the response if needed
        // For example, if the response contains the created user with an ID:
        User createdUser = objectMapper.readValue(result.andReturn().getResponse().getContentAsString(), User.class);
        // Additional assertions or checks based on the createdUser
    }

    @Test
    public void shouldUpdateUser() throws Exception {
        // Assuming there is a user with id 1 in your test data
        long userId = 1L;

        User updatedUser = new User("updateduser", "newpassword", "Updated User");

        mockMvc.perform(put("/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedUser)))
                .andExpect(status().isOk());

        // Additional assertions or checks if needed
    }

    @Test
    public void shouldDeleteUser() throws Exception {
        // Assuming there is a user with id 1 in your test data
        long userId = 1L;

        mockMvc.perform(delete("/users/{id}", userId))
                .andExpect(status().isOk());

        // Additional assertions or checks if needed
    }
}