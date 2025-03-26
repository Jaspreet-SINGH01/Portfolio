package com.videoflix.Users.validation;

import com.videoflix.users_microservice.repositories.UserRepository;
import com.videoflix.users_microservice.validation.annotations.UniqueUsername;
import com.videoflix.users_microservice.validation.validators.UniqueUsernameValidator;
import com.videoflix.users_microservice.entities.User;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class) // Active les annotations Mockito
class UniqueUsernameValidatorTest {

    @Mock
    private UserRepository userRepository; // Mock pour UserRepository

    @Mock
    private UniqueUsername uniqueUsernameAnnotation; // Mock pour l'annotation UniqueUsername (non utilisé dans ce test,
                                                     // mais utile pour des tests plus complexes)

    @Mock
    private ConstraintValidatorContext constraintValidatorContext; // Mock pour ConstraintValidatorContext (non utilisé
                                                                   // dans ce test, mais utile pour des tests plus
                                                                   // complexes)

    private UniqueUsernameValidator uniqueUsernameValidator; // Instance du validateur à tester

    @BeforeEach
    void setUp() {
        uniqueUsernameValidator = new UniqueUsernameValidator(userRepository); // Initialisation du validateur avec le
                                                                               // mock UserRepository
    }

    @Test
    void isValid_ShouldReturnTrue_WhenUsernameIsUnique() {
        // Configuration du mock UserRepository pour simuler un nom d'utilisateur unique
        when(userRepository.findByUsername("uniqueUsername")).thenReturn(Optional.empty());

        // Appel de la méthode à tester
        boolean isValid = uniqueUsernameValidator.isValid("uniqueUsername", constraintValidatorContext);

        // Vérification que le résultat est vrai
        assertTrue(isValid);
    }

    @Test
    void isValid_ShouldReturnFalse_WhenUsernameIsDuplicate() {
        // Configuration du mock UserRepository pour simuler un nom d'utilisateur
        // dupliqué
        when(userRepository.findByUsername("duplicateUsername")).thenReturn(Optional.of(new User())); // Simule la
                                                                                                      // présence d'un
                                                                                                      // utilisateur

        // Appel de la méthode à tester
        boolean isValid = uniqueUsernameValidator.isValid("duplicateUsername", constraintValidatorContext);

        // Vérification que le résultat est faux
        assertFalse(isValid);
    }
}