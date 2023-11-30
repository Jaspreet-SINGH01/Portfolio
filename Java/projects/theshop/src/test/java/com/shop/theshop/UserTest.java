package com.shop.theshop;

import com.shop.theshop.entities.User;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class UserTest {

    @Test
    public void shouldCreateUser() {
        // Given
        Long userId = 1L;
        String username = "testuser";
        String password = "password123";
        String name = "Test User";

        // When
        User user = new User();
        user.setId(userId);
        user.setUsername(username);
        user.setPassword(password);
        user.setName(name);

        // Then
        assertThat(user.getId()).isEqualTo(userId);
        assertThat(user.getUsername()).isEqualTo(username);
        assertThat(user.getPassword()).isEqualTo(password);
        assertThat(user.getName()).isEqualTo(name);
    }
}