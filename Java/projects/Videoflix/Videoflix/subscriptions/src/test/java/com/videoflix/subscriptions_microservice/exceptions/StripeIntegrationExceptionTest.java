package com.videoflix.subscriptions_microservice.exceptions;

import org.junit.jupiter.api.Test;

import com.stripe.exception.StripeException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class StripeIntegrationExceptionTest {

    // Test pour vérifier la création de l'exception avec un message uniquement
    @Test
    void stripeIntegrationException_shouldBeCreatedWithMessageOnly() {
        // GIVEN : Un message indiquant une erreur d'intégration avec Stripe
        String message = "Erreur lors de la communication avec Stripe.";

        // WHEN : Création d'une instance de StripeIntegrationException avec le
        // constructeur à un seul argument
        StripeIntegrationException exception = new StripeIntegrationException(message);

        // THEN : Vérification que le message est correctement défini et que la cause
        // est nulle
        assertEquals(message, exception.getMessage(), "Le message de l'exception doit correspondre.");
        assertNull(exception.getCause(), "La cause de l'exception devrait être nulle.");
    }

    // Test pour vérifier la création de l'exception avec un message et une cause
    @Test
    void stripeIntegrationException_shouldBeCreatedWithMessageAndCause() {
        // GIVEN : Un message indiquant une erreur d'intégration avec Stripe et une
        // exception cause
        String message = "Une erreur s'est produite lors de la création du client Stripe.";
        Throwable causeException = new StripeException(message, message, message, null) {
        };

        // WHEN : Création d'une instance de StripeIntegrationException avec le
        // constructeur à deux arguments
        StripeIntegrationException exception = new StripeIntegrationException(message, causeException);

        // THEN : Vérification que le message et la cause sont correctement définis
        assertEquals(message, exception.getMessage(), "Le message de l'exception doit correspondre.");
        assertEquals(causeException, exception.getCause(), "La cause de l'exception doit correspondre.");
    }
}