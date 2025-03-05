package com.shop.theshop;

import com.shop.theshop.entities.User;
import com.shop.theshop.repositories.UserRepository;
import com.shop.theshop.services.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @MockBean
    private UserRepository userRepository;

    @Test
    public void shouldGetUserById() {
        long userId = 1L;
        User mockUser = new User("testuser", "password123", "Test User");
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        User retrievedUser = userService.findById(userId);

        assertThat(retrievedUser).isNotNull();
        assertThat(retrievedUser.getFirstname()).isEqualTo("testuser");
        assertThat(retrievedUser.getName()).isEqualTo("Test User");
    }

    @Test
    public void shouldCreateUser() {
        User newUser = new User("newuser", "newpassword", "New User");
        when(userRepository.save(newUser)).thenReturn(newUser);

        User createdUser = userService.createUser(newUser);

        assertThat(createdUser).isNotNull();
        assertThat(createdUser.getFirstname()).isEqualTo("newuser");
        assertThat(createdUser.getName()).isEqualTo("New User");
    }

    @Test
    public void shouldUpdateUser() {
        long userId = 1L;
        User existingUser = new User("existinguser", "oldpassword", "Existing User");
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

        User updatedUser = new User("updateduser", "newpassword", "Updated User");

        User updatedUserResult = userService.updateUser(userId, updatedUser);

        assertThat(updatedUserResult).isNotNull();
        assertThat(updatedUserResult.getFirstname()).isEqualTo("updateduser");
        assertThat(updatedUserResult.getName()).isEqualTo("Updated User");
    }

    @Test
    public void shouldDeleteUser() {
        long userId = 1L;
        User existingUser = new User("existinguser", "oldpassword", "Existing User");
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

        userService.deleteUser(userId);
    }
}