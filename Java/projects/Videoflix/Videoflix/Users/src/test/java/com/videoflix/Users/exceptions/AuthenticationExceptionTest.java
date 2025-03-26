package com.videoflix.Users.exceptions;

import org.junit.jupiter.api.Test;

import com.videoflix.users_microservice.exceptions.AuthenticationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthenticationExceptionTest {

    @Test
    void authenticationException_ShouldCreateExceptionWithCorrectMessage() {
        // Teste que l'exception AuthenticationException est créée avec le message
        // correct

        // Message de test
        String errorMessage = "Erreur d'authentification test";

        // Création de l'exception
        AuthenticationException exception = new AuthenticationException(errorMessage);

        // Vérification que le message de l'exception est correct
        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void authenticationException_ShouldBeRuntimeException() {
        // Teste que AuthenticationException est une sous-classe de RuntimeException

        // Vérification que AuthenticationException est une sous-classe de
        // RuntimeException
        assertTrue(RuntimeException.class.isAssignableFrom(AuthenticationException.class));
    }

    @Test
    void authenticationException_ShouldBeThrownAndCaught() {
        // Teste que AuthenticationException peut être lancée et capturée

        // Message de test
        String errorMessage = "Erreur d'authentification test";

        // Vérification que l'exception est lancée et capturée correctement
        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            throw new AuthenticationException(errorMessage);
        });

        // Vérification que le message de l'exception capturée est correct
        assertEquals(errorMessage, exception.getMessage());
    }
}