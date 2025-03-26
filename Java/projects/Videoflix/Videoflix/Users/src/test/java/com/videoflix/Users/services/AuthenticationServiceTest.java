package com.videoflix.Users.services;

import com.videoflix.users_microservice.config.JwtConfig;
import com.videoflix.users_microservice.entities.User;
import com.videoflix.users_microservice.exceptions.AuthenticationException;
import com.videoflix.users_microservice.repositories.UserRepository;
import com.videoflix.users_microservice.services.AuthenticationService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthenticationServiceTest {

    // Mocks pour simuler les dépendances
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtConfig jwtConfig;

    // Service à tester
    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        // Initialisation des mocks avant chaque test
        MockitoAnnotations.openMocks(this);

        // Création de l'instance du service à tester
        authenticationService = new AuthenticationService(
                userRepository,
                passwordEncoder,
                jwtConfig);
    }

    @Test
    void testAuthenticate_SuccessfulAuthentication() {
        // Préparation des données de test
        String username = "testuser";
        String rawPassword = "password123";
        String encodedPassword = "encodedPassword";

        // Création d'un utilisateur mock
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername(username);
        mockUser.setPassword(encodedPassword);

        // Configuration des comportements simulés
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);
        when(jwtConfig.getExpiration()).thenReturn(3600000L); // 1 heure
        when(jwtConfig.getSecret()).thenReturn("testSecret");

        // Exécution de la méthode à tester
        String token = authenticationService.authenticate(username, rawPassword);

        // Vérifications
        assertNotNull(token, "Le token ne doit pas être null");

        // Vérification des interactions avec les mocks
        verify(userRepository).findByUsername(username);
        verify(passwordEncoder).matches(rawPassword, encodedPassword);
    }

    @Test
    void testAuthenticate_UserNotFound() {
        // Préparation des données de test
        String username = "nonexistentuser";
        String password = "password123";

        // Configuration du comportement simulé
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // Vérification que l'exception est levée
        AuthenticationException exception = assertThrows(
                AuthenticationException.class,
                () -> authenticationService.authenticate(username, password),
                "Une AuthenticationException doit être levée pour un utilisateur inexistant");

        // Vérification du message d'erreur
        assertEquals(
                "Nom d'utilisateur ou mot de passe incorrect.",
                exception.getMessage(),
                "Le message d'erreur doit correspondre");
    }

    @Test
    void testAuthenticate_InvalidPassword() {
        // Préparation des données de test
        String username = "testuser";
        String rawPassword = "wrongpassword";
        String encodedPassword = "correctEncodedPassword";

        // Création d'un utilisateur mock
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername(username);
        mockUser.setPassword(encodedPassword);

        // Configuration des comportements simulés
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(false);

        // Vérification que l'exception est levée
        AuthenticationException exception = assertThrows(
                AuthenticationException.class,
                () -> authenticationService.authenticate(username, rawPassword),
                "Une AuthenticationException doit être levée pour un mot de passe incorrect");

        // Vérification du message d'erreur
        assertEquals(
                "Nom d'utilisateur ou mot de passe incorrect.",
                exception.getMessage(),
                "Le message d'erreur doit correspondre");
    }
}