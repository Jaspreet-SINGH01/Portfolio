package com.videoflix.Users.controller;

import com.videoflix.users_microservice.controller.UserController;
import com.videoflix.users_microservice.dto.UpdateUserDTO;
import com.videoflix.users_microservice.dto.UserDTO;
import com.videoflix.users_microservice.entities.Role;
import com.videoflix.users_microservice.entities.User;
import com.videoflix.users_microservice.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;

class UserControllerTest {

    // Simulation du service utilisateur
    @Mock
    private UserService userService;

    // Contrôleur à tester
    private UserController userController;

    // Simulation des détails utilisateur
    @Mock
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        // Initialisation des mocks
        MockitoAnnotations.openMocks(this);
        userController = new UserController(userService);
    }

    @Test
    void testGetUserProfile() {
        // Préparation des données de test
        Long userId = 1L;
        User mockUser = new User();
        mockUser.setId(userId);
        mockUser.setName("Test User");
        mockUser.setEmail("test@example.com");

        // Configuration du comportement simulé
        when(userDetails.getUsername()).thenReturn(String.valueOf(userId));
        when(userService.getUserById(userId)).thenReturn(mockUser);

        // Exécution de la méthode à tester
        ResponseEntity<UserDTO> response = userController.getUserProfile(userDetails);

        // Vérifications
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(userId, response.getBody().getId());
        assertEquals("Test User", response.getBody().getUsername());
        assertEquals("test@example.com", response.getBody().getEmail());

        // Vérification des interactions
        verify(userService).getUserById(userId);
    }

    @Test
    void testCreateUser() {
        // Préparation des données de test
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("New User");
        userDTO.setEmail("new@example.com");
        userDTO.setPassword("password123");

        User createdUser = new User();
        createdUser.setId(1L);
        createdUser.setName("New User");
        createdUser.setEmail("new@example.com");

        // Configuration du comportement simulé
        when(userService.createUser(any(User.class), eq(Role.USER))).thenReturn(createdUser);

        // Exécution de la méthode à tester
        ResponseEntity<UserDTO> response = userController.createUser(userDTO);

        // Vérifications
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("New User", response.getBody().getUsername());
        assertEquals("new@example.com", response.getBody().getEmail());

        // Vérification des interactions
        verify(userService).createUser(any(User.class), eq(Role.USER));
    }

    @Test
    void testUpdateUser() {
        // Préparation des données de test
        Long userId = 1L;
        UpdateUserDTO updateDTO = new UpdateUserDTO();
        updateDTO.setName("Updated Name");
        updateDTO.setEmail("updated@example.com");

        // Configuration du comportement simulé
        when(userDetails.getUsername()).thenReturn(String.valueOf(userId));
        when(userDetails.getAuthorities()).thenReturn(any());

        User updatedUser = new User();
        updatedUser.setId(userId);
        updatedUser.setName("Updated Name");
        updatedUser.setEmail("updated@example.com");

        when(userService.updateUser(
                userId,
                "Updated Name",
                "updated@example.com",
                Role.USER)).thenReturn(updatedUser);

        // Exécution de la méthode à tester
        ResponseEntity<UserDTO> response = userController.updateUser(userDetails, updateDTO);

        // Vérifications
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Updated Name", response.getBody().getUsername());
        assertEquals("updated@example.com", response.getBody().getEmail());

        // Vérification des interactions
        verify(userService).updateUser(
                userId,
                "Updated Name",
                "updated@example.com",
                Role.USER);
    }

    @Test
    void testDeleteUser_Authorized() {
        // Préparation des données de test
        Long userId = 1L;

        // Configuration du comportement simulé
        when(userDetails.getUsername()).thenReturn(String.valueOf(userId));

        // Exécution de la méthode à tester
        ResponseEntity<Void> response = userController.deleteUser(userId, true, userDetails);

        // Vérifications
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        // Vérification des interactions
        verify(userService).deleteUser(eq(userId), (User) userDetails);
    }

    @Test
    void testDeleteUser_Unauthorized() {
        // Préparation des données de test
        Long userId = 1L;
        Long differentUserId = 2L;

        // Configuration du comportement simulé
        when(userDetails.getUsername()).thenReturn(String.valueOf(differentUserId));

        // Exécution de la méthode à tester
        ResponseEntity<Void> response = userController.deleteUser(userId, true, userDetails);

        // Vérifications
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());

        // Vérification de l'absence d'appel au service
        verify(userService, never()).deleteUser(eq(userId), (User) userDetails);
    }

    @Test
    void testForgotPassword() {
        // Préparation des données de test
        String email = "test@example.com";

        // Exécution de la méthode à tester
        ResponseEntity<Void> response = userController.forgotPassword(email);

        // Vérifications
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // Vérification des interactions
        verify(userService).generateResetToken(email);
    }

    @Test
    void testForgotPassword_InvalidEmail() {
        // Exécution de la méthode à tester
        ResponseEntity<Void> response = userController.forgotPassword("");

        // Vérifications
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        // Vérification de l'absence d'appel au service
        verify(userService, never()).generateResetToken(anyString());
    }
}