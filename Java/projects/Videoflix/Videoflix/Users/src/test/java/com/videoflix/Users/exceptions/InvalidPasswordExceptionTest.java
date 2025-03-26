package com.videoflix.Users.exceptions;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.videoflix.users_microservice.exceptions.InvalidPasswordException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InvalidPasswordExceptionTest {

    @Test
    void invalidPasswordException_ShouldCreateExceptionWithCorrectMessage() {
        // Teste que l'exception InvalidPasswordException est créée avec le message correct

        // Message de test
        String errorMessage = "Mot de passe invalide test";

        // Création de l'exception
        InvalidPasswordException exception = new InvalidPasswordException(errorMessage);

        // Vérification que le message de l'exception est correct
        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void invalidPasswordException_ShouldBeRuntimeException() {
        // Teste que InvalidPasswordException est une sous-classe de RuntimeException

        // Vérification que InvalidPasswordException est une sous-classe de RuntimeException
        assertTrue(RuntimeException.class.isAssignableFrom(InvalidPasswordException.class));
    }

    @Test
    void invalidPasswordException_ShouldBeThrownAndCaught() {
        // Teste que InvalidPasswordException peut être lancée et capturée

        // Message de test
        String errorMessage = "Mot de passe invalide test";

        // Vérification que l'exception est lancée et capturée correctement
        InvalidPasswordException exception = assertThrows(InvalidPasswordException.class, () -> {
            throw new InvalidPasswordException(errorMessage);
        });

        // Vérification que le message de l'exception capturée est correct
        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void invalidPasswordException_ShouldHaveCorrectResponseStatus() {
        // Teste que l'exception InvalidPasswordException a le code de statut HTTP correct

        // Récupération de l'annotation ResponseStatus de la classe InvalidPasswordException
        ResponseStatus responseStatus = InvalidPasswordException.class.getAnnotation(ResponseStatus.class);

        // Vérification que l'annotation ResponseStatus est présente
        assertNotNull(responseStatus);

        // Vérification que le code de statut HTTP est BAD_REQUEST
        assertEquals(HttpStatus.BAD_REQUEST, responseStatus.value());
    }
}