package com.videoflix.Users.controller;

import com.videoflix.users_microservice.controller.OAuth2Controller;
import com.videoflix.users_microservice.dto.UserDTO;
import com.videoflix.users_microservice.entities.User;
import com.videoflix.users_microservice.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.security.Principal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OAuth2ControllerTest {

    // Simulation du repository utilisateur
    @Mock
    private UserRepository userRepository;

    // Simulation de l'utilisateur OAuth2
    @Mock
    private OAuth2User oauth2User;

    // Contrôleur à tester
    private OAuth2Controller oauth2Controller;

    @BeforeEach
    void setUp() {
        // Initialisation des mocks
        MockitoAnnotations.openMocks(this);
        oauth2Controller = new OAuth2Controller(userRepository);
    }

    @Test
    void testOauth2Success_NewUser() {
        // Préparation des données de test pour un nouvel utilisateur
        String email = "newuser@example.com";
        String name = "New User";

        // Configuration du comportement simulé de l'utilisateur OAuth2
        when(oauth2User.getAttribute("email")).thenReturn(email);
        when(oauth2User.getAttribute("name")).thenReturn(name);

        // Configuration du comportement du repository
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Simulation de l'authentification
        ResponseEntity<UserDTO> response = oauth2Controller.oauth2Success((Principal) oauth2User);

        // Vérifications
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(email, response.getBody().getEmail());
        assertEquals(name, response.getBody().getUsername());

        // Vérification des interactions avec le repository
        verify(userRepository).findByEmail(email);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testOauth2Success_ExistingUser() {
        // Préparation des données de test pour un utilisateur existant
        String email = "existinguser@example.com";
        String newName = "Updated Name";

        // Création d'un utilisateur existant
        User existingUser = new User();
        existingUser.setEmail(email);
        existingUser.setName("Old Name");

        // Configuration du comportement simulé de l'utilisateur OAuth2
        when(oauth2User.getAttribute("email")).thenReturn(email);
        when(oauth2User.getAttribute("name")).thenReturn(newName);

        // Configuration du comportement du repository
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Simulation de l'authentification
        ResponseEntity<UserDTO> response = oauth2Controller.oauth2Success((Principal) oauth2User);

        // Vérifications
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(email, response.getBody().getEmail());
        assertEquals(newName, response.getBody().getUsername());

        // Vérification des interactions avec le repository
        verify(userRepository).findByEmail(email);
        verify(userRepository).save(existingUser);
    }

    @Test
    void testOauth2Success_MissingAttributes() {
        // Configuration du comportement simulé de l'utilisateur OAuth2 avec des
        // attributs manquants
        when(oauth2User.getAttribute("email")).thenReturn(null);
        when(oauth2User.getAttribute("name")).thenReturn(null);

        // Simulation de l'authentification
        ResponseEntity<UserDTO> response = oauth2Controller.oauth2Success((Principal) oauth2User);

        // Vérifications
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void testOauth2Success_AuthenticationException() {
        // Simulation d'une exception d'authentification
        ResponseEntity<UserDTO> response = oauth2Controller.oauth2Success(new Principal() {
            @Override
            public String getName() {
                throw new OAuth2AuthenticationException("Authentication failed");
            }
        });

        // Vérifications
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());
    }
}