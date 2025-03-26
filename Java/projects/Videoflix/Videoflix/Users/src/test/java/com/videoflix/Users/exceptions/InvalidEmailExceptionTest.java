package com.videoflix.Users.exceptions;

import org.junit.jupiter.api.Test;

import com.videoflix.users_microservice.exceptions.InvalidEmailException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InvalidEmailExceptionTest {

    @Test
    void invalidEmailException_ShouldCreateExceptionWithCorrectMessage() {
        // Teste que l'exception InvalidEmailException est créée avec le message correct

        // Message de test
        String errorMessage = "Adresse email invalide test";

        // Création de l'exception
        InvalidEmailException exception = new InvalidEmailException(errorMessage);

        // Vérification que le message de l'exception est correct
        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void invalidEmailException_ShouldBeRuntimeException() {
        // Teste que InvalidEmailException est une sous-classe de RuntimeException

        // Vérification que InvalidEmailException est une sous-classe de
        // RuntimeException
        assertTrue(RuntimeException.class.isAssignableFrom(InvalidEmailException.class));
    }

    @Test
    void invalidEmailException_ShouldBeThrownAndCaught() {
        // Teste que InvalidEmailException peut être lancée et capturée

        // Message de test
        String errorMessage = "Adresse email invalide test";

        // Vérification que l'exception est lancée et capturée correctement
        InvalidEmailException exception = assertThrows(InvalidEmailException.class, () -> {
            throw new InvalidEmailException(errorMessage);
        });

        // Vérification que le message de l'exception capturée est correct
        assertEquals(errorMessage, exception.getMessage());
    }
}