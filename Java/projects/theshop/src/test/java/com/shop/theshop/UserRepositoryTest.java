package com.shop.theshop;

import com.shop.theshop.entities.User;
import com.shop.theshop.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    public void shouldSaveAndRetrieveUser() {
        // Given
        User user = new User("testuser", "password123", "Test User");

        // When
        userRepository.save(user);

        // Then
        Optional<User> retrievedUserOptional = userRepository.findById(user.getId());
        assertThat(retrievedUserOptional).isPresent();

        User retrievedUser = retrievedUserOptional.get();
        assertThat(retrievedUser.getUsername()).isEqualTo("testuser");
        assertThat(retrievedUser.getPassword()).isEqualTo("password123");
        assertThat(retrievedUser.getName()).isEqualTo("Test User");
    }

    @Test
    public void shouldFindByUsername() {
        // Given
        String username = "testuser";
        User user = new User(username, "password123", "Test User");
        userRepository.save(user);

        // When
        Optional<User> retrievedUserOptional = userRepository.findByUsername(username);

        // Then
        assertThat(retrievedUserOptional).isPresent();
        User retrievedUser = retrievedUserOptional.get();
        assertThat(retrievedUser.getUsername()).isEqualTo(username);
    }

    @Test
    public void shouldDeleteUser() {
        // Given
        User user = new User("testuser", "password123", "Test User");
        userRepository.save(user);

        // When
        userRepository.deleteById(user.getId());

        // Then
        Optional<User> retrievedUserOptional = userRepository.findById(user.getId());
        assertThat(retrievedUserOptional).isEmpty();
    }
}
