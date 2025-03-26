package com.videoflix.Users.exceptions;

import org.junit.jupiter.api.Test;

import com.videoflix.users_microservice.exceptions.AuthorizationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthorizationExceptionTest {

    @Test
    void authorizationException_ShouldCreateExceptionWithCorrectMessage() {
        // Teste que l'exception AuthorizationException est créée avec le message
        // correct

        // Message de test
        String errorMessage = "Erreur d'autorisation test";

        // Création de l'exception
        AuthorizationException exception = new AuthorizationException(errorMessage);

        // Vérification que le message de l'exception est correct
        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void authorizationException_ShouldBeRuntimeException() {
        // Teste que AuthorizationException est une sous-classe de RuntimeException

        // Vérification que AuthorizationException est une sous-classe de
        // RuntimeException
        assertTrue(RuntimeException.class.isAssignableFrom(AuthorizationException.class));
    }

    @Test
    void authorizationException_ShouldBeThrownAndCaught() {
        // Teste que AuthorizationException peut être lancée et capturée

        // Message de test
        String errorMessage = "Erreur d'autorisation test";

        // Vérification que l'exception est lancée et capturée correctement
        AuthorizationException exception = assertThrows(AuthorizationException.class, () -> {
            throw new AuthorizationException(errorMessage);
        });

        // Vérification que le message de l'exception capturée est correct
        assertEquals(errorMessage, exception.getMessage());
    }
}