package com.videoflix.Users.exceptions;

import org.junit.jupiter.api.Test;

import com.videoflix.users_microservice.exceptions.EmailSendingException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailSendingExceptionTest {

    @Test
    void emailSendingException_ShouldCreateExceptionWithCorrectMessageAndCause() {
        // Teste que l'exception EmailSendingException est créée avec le message et la
        // cause corrects

        // Message de test
        String errorMessage = "Erreur d'envoi d'email test";

        // Cause de test
        Throwable cause = new RuntimeException("Cause de test");

        // Création de l'exception
        EmailSendingException exception = new EmailSendingException(errorMessage, cause);

        // Vérification que le message de l'exception est correct
        assertEquals(errorMessage, exception.getMessage());

        // Vérification que la cause de l'exception est correcte
        assertEquals(cause, exception.getCause());
    }

    @Test
    void emailSendingException_ShouldBeRuntimeException() {
        // Teste que EmailSendingException est une sous-classe de RuntimeException

        // Vérification que EmailSendingException est une sous-classe de
        // RuntimeException
        assertTrue(RuntimeException.class.isAssignableFrom(EmailSendingException.class));
    }

    @Test
    void emailSendingException_ShouldBeThrownAndCaught() {
        // Teste que EmailSendingException peut être lancée et capturée

        // Message de test
        String errorMessage = "Erreur d'envoi d'email test";

        // Cause de test
        Throwable cause = new RuntimeException("Cause de test");

        // Vérification que l'exception est lancée et capturée correctement
        EmailSendingException exception = assertThrows(EmailSendingException.class, () -> {
            throw new EmailSendingException(errorMessage, cause);
        });

        // Vérification que le message de l'exception capturée est correct
        assertEquals(errorMessage, exception.getMessage());

        // Vérification que la cause de l'exception capturée est correcte
        assertEquals(cause, exception.getCause());
    }
}