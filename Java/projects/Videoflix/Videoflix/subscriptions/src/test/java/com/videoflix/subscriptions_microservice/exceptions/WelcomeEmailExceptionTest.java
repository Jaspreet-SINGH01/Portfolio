package com.videoflix.subscriptions_microservice.exceptions;

import org.junit.jupiter.api.Test;
import org.springframework.messaging.MessagingException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class WelcomeEmailExceptionTest {

    // Test pour vérifier la création de l'exception avec un message et une cause
    @Test
    void welcomeEmailException_shouldBeCreatedWithMessageAndCause() {
        // GIVEN : Un message décrivant l'erreur d'envoi de l'e-mail de bienvenue et une
        // exception cause
        String message = "Erreur lors de l'envoi de l'e-mail de bienvenue.";
        Throwable causeException = new MessagingException("Impossible de se connecter au serveur SMTP.");

        // WHEN : Création d'une instance de WelcomeEmailException avec le constructeur
        // à deux arguments
        WelcomeEmailException exception = new WelcomeEmailException(message, causeException);

        // THEN : Vérification que le message et la cause sont correctement définis
        assertEquals(message, exception.getMessage(), "Le message de l'exception doit correspondre.");
        assertEquals(causeException, exception.getCause(), "La cause de l'exception doit correspondre.");
    }

    // Test pour vérifier la création de l'exception avec un message et une cause
    // nulle
    @Test
    void welcomeEmailException_shouldBeCreatedWithMessageAndNullCause() {
        // GIVEN : Un message décrivant l'erreur d'envoi de l'e-mail de bienvenue et une
        // cause nulle
        String message = "L'envoi de l'e-mail de bienvenue a échoué pour une raison inconnue.";
        Throwable causeException = null;

        // WHEN : Création d'une instance de WelcomeEmailException avec le constructeur
        // à deux arguments
        WelcomeEmailException exception = new WelcomeEmailException(message, causeException);

        // THEN : Vérification que le message est correctement défini et que la cause
        // est nulle
        assertEquals(message, exception.getMessage(), "Le message de l'exception doit correspondre.");
        assertNull(exception.getCause(), "La cause de l'exception devrait être nulle.");
    }
}