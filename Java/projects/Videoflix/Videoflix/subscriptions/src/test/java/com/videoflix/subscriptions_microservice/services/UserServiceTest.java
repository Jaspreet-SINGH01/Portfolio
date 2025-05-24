package com.videoflix.subscriptions_microservice.services;

import com.videoflix.subscriptions_microservice.entities.User;
import com.videoflix.subscriptions_microservice.repositories.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// @ExtendWith(MockitoExtension.class) permet d'initialiser les mocks automatiquement avant chaque test.
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests pour UserService")
class UserServiceTest {
    // @Mock crée une instance mockée de UserRepository.
    @Mock
    private UserRepository userRepository;

    // @InjectMocks crée une instance de UserService et injecte automatiquement
    // les mocks (ici, userRepository) dans son constructeur.
    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("Devrait retourner un utilisateur quand un customerId Stripe est trouvé")
    void findByStripeCustomerId_shouldReturnUserWhenFound() {
        // GIVEN: Un ID client Stripe et un utilisateur correspondant.
        String customerId = "cus_abc123";
        User expectedUser = new User();
        expectedUser.setId(1L);
        expectedUser.setEmail("test@example.com");
        expectedUser.setStripeCustomerId(customerId);

        // WHEN: Le userRepository.findByStripeCustomerId est appelé, il doit retourner
        // cet utilisateur.
        // `Optional.of(expectedUser)` simule que l'utilisateur a été trouvé en base.
        when(userRepository.findByStripeCustomerId(customerId)).thenReturn(Optional.of(expectedUser));

        // WHEN: On appelle la méthode du service que l'on veut tester.
        User actualUser = userService.findByStripeCustomerId(customerId);

        // THEN: On vérifie que la méthode du repository a bien été appelée une fois
        // avec le bon ID.
        verify(userRepository, times(1)).findByStripeCustomerId(customerId);
        // On vérifie que l'utilisateur retourné par le service est bien celui que l'on
        // attend.
        assertNotNull(actualUser, "L'utilisateur ne devrait pas être null.");
        assertEquals(expectedUser.getId(), actualUser.getId(), "L'ID de l'utilisateur doit correspondre.");
        assertEquals(expectedUser.getEmail(), actualUser.getEmail(), "L'email de l'utilisateur doit correspondre.");
        assertEquals(expectedUser.getStripeCustomerId(), actualUser.getStripeCustomerId(),
                "Le customerId Stripe doit correspondre.");
    }

    @Test
    @DisplayName("Devrait retourner null quand aucun customerId Stripe n'est trouvé")
    void findByStripeCustomerId_shouldReturnNullWhenNotFound() {
        // GIVEN: Un ID client Stripe pour lequel aucun utilisateur n'existe.
        String customerId = "cus_nonexistent";

        // WHEN: Le userRepository.findByStripeCustomerId est appelé, il doit retourner
        // un Optional vide.
        // `Optional.empty()` simule que l'utilisateur n'a pas été trouvé en base.
        when(userRepository.findByStripeCustomerId(customerId)).thenReturn(Optional.empty());

        // WHEN: On appelle la méthode du service que l'on veut tester.
        User actualUser = userService.findByStripeCustomerId(customerId);

        // THEN: On vérifie que la méthode du repository a bien été appelée une fois
        // avec le bon ID.
        verify(userRepository, times(1)).findByStripeCustomerId(customerId);
        // On vérifie que l'utilisateur retourné par le service est bien null, comme
        // attendu.
        assertNull(actualUser, "L'utilisateur devrait être null si aucun n'est trouvé.");
    }

    @Test
    @DisplayName("Devrait gérer un customerId Stripe null en retournant null")
    void findByStripeCustomerId_shouldReturnNullWhenCustomerIdIsNull() {
        // GIVEN: Un ID client Stripe null.

        // WHEN: On appelle la méthode du service avec un customerId null.
        // Il est important de noter que nous ne configurons pas de mock pour le
        // repository ici,
        // car le service devrait gérer ce cas avant même d'appeler le repository si
        // nécessaire.
        // Cependant, dans l'implémentation actuelle, il appellera
        // `userRepository.findByStripeCustomerId(null)`.
        // C'est au repository de décider comment gérer un `null` (généralement, il
        // retournera un Optional vide).
        when(userRepository.findByStripeCustomerId(null)).thenReturn(Optional.empty());

        User actualUser = userService.findByStripeCustomerId(null);

        // THEN: On vérifie que la méthode du repository a été appelée.
        verify(userRepository, times(1)).findByStripeCustomerId(null);
        // On vérifie que l'utilisateur retourné est null.
        assertNull(actualUser, "L'utilisateur devrait être null si le customerId est null.");
    }

    @Test
    @DisplayName("Devrait gérer un customerId Stripe vide en retournant null")
    void findByStripeCustomerId_shouldReturnNullWhenCustomerIdIsEmpty() {
        // GIVEN: Un ID client Stripe vide.
        String customerId = "";

        // WHEN: Le repository est configuré pour retourner un Optional vide si l'ID est
        // vide.
        when(userRepository.findByStripeCustomerId(customerId)).thenReturn(Optional.empty());

        // WHEN: On appelle la méthode du service avec un customerId vide.
        User actualUser = userService.findByStripeCustomerId(customerId);

        // THEN: On vérifie que la méthode du repository a été appelée.
        verify(userRepository, times(1)).findByStripeCustomerId(customerId);
        // On vérifie que l'utilisateur retourné est null.
        assertNull(actualUser, "L'utilisateur devrait être null si le customerId est vide.");
    }
}