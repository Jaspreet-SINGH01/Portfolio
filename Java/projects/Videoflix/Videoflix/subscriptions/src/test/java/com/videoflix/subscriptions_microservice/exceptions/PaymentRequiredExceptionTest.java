package com.videoflix.subscriptions_microservice.exceptions;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PaymentRequiredExceptionTest {

    // Test pour vérifier la création de l'exception avec un message uniquement
    @Test
    void paymentRequiredException_shouldBeCreatedWithMessageOnly() {
        // GIVEN : Un message indiquant qu'un paiement est requis
        String message = "Un paiement est requis pour accéder à cette fonctionnalité.";

        // WHEN : Création d'une instance de PaymentRequiredException avec le
        // constructeur à un seul argument
        PaymentRequiredException exception = new PaymentRequiredException(message);

        // THEN : Vérification que le message est correctement défini et que la cause
        // est nulle
        assertEquals(message, exception.getMessage(), "Le message de l'exception doit correspondre.");
        assertNull(exception.getCause(), "La cause de l'exception devrait être nulle.");
    }

    // Test pour vérifier la création de l'exception avec un message et une cause
    @Test
    void paymentRequiredException_shouldBeCreatedWithMessageAndCause() {
        // GIVEN : Un message indiquant qu'un paiement est requis et une exception cause
        String message = "Le paiement a échoué, veuillez réessayer.";
        Throwable causeException = new PaymentFailedException("Erreur lors du traitement du paiement.");

        // WHEN : Création d'une instance de PaymentRequiredException avec le
        // constructeur à deux arguments
        PaymentRequiredException exception = new PaymentRequiredException(message, causeException);

        // THEN : Vérification que le message et la cause sont correctement définis
        assertEquals(message, exception.getMessage(), "Le message de l'exception doit correspondre.");
        assertEquals(causeException, exception.getCause(), "La cause de l'exception doit correspondre.");
    }

    // Test pour vérifier l'annotation @ResponseStatus sur la classe
    @Test
    void paymentRequiredException_shouldHavePaymentRequiredStatus() {
        // GIVEN : La classe PaymentRequiredException est annotée avec @ResponseStatus

        // WHEN : Récupération de l'annotation ResponseStatus de la classe
        ResponseStatus responseStatus = PaymentRequiredException.class.getAnnotation(ResponseStatus.class);

        // THEN : Vérification que l'annotation est présente et que sa valeur est
        // HttpStatus.PAYMENT_REQUIRED
        assertEquals(HttpStatus.PAYMENT_REQUIRED, responseStatus.value(),
                "L'exception devrait avoir le statut HTTP PAYMENT_REQUIRED.");
    }
}