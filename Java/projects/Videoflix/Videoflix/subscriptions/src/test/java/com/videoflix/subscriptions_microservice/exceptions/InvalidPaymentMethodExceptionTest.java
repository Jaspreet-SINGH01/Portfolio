package com.videoflix.subscriptions_microservice.exceptions;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class InvalidPaymentMethodExceptionTest {

    // Test pour vérifier la création de l'exception avec un message uniquement
    @Test
    void invalidPaymentMethodException_shouldBeCreatedWithMessageOnly() {
        // GIVEN : Un message d'erreur indiquant une méthode de paiement invalide
        String errorMessage = "Méthode de paiement invalide.";

        // WHEN : Création d'une instance de InvalidPaymentMethodException avec le
        // constructeur à un seul argument
        InvalidPaymentMethodException exception = new InvalidPaymentMethodException(errorMessage);

        // THEN : Vérification que le message est correctement défini et que la méthode
        // de paiement fournie est initialisée par défaut
        assertEquals(errorMessage, exception.getMessage(), "Le message de l'exception doit correspondre.");
        assertNull(exception.getCause(), "La cause de l'exception devrait être nulle.");
        assertEquals("", exception.getProvidedPaymentMethod(),
                "La méthode de paiement fournie devrait être une chaîne vide.");
    }

    // Test pour vérifier la création de l'exception avec un message et une cause
    @Test
    void invalidPaymentMethodException_shouldBeCreatedWithMessageAndCause() {
        // GIVEN : Un message d'erreur et une exception cause
        String errorMessage = "Erreur lors de la validation de la méthode de paiement.";
        Throwable causeException = new IllegalArgumentException("Type de carte non supporté.");

        // WHEN : Création d'une instance de InvalidPaymentMethodException avec le
        // constructeur à deux arguments
        InvalidPaymentMethodException exception = new InvalidPaymentMethodException(errorMessage, causeException);

        // THEN : Vérification que le message et la cause sont correctement définis et
        // que la méthode de paiement fournie est initialisée par défaut
        assertEquals(errorMessage, exception.getMessage(), "Le message de l'exception doit correspondre.");
        assertEquals(causeException, exception.getCause(), "La cause de l'exception doit correspondre.");
        assertEquals("", exception.getProvidedPaymentMethod(),
                "La méthode de paiement fournie devrait être une chaîne vide.");
    }

    // Test pour vérifier la création de l'exception avec un message et la méthode
    // de paiement fournie
    @Test
    void invalidPaymentMethodException_shouldBeCreatedWithMessageAndProvidedMethod() {
        // GIVEN : Un message d'erreur et la méthode de paiement fournie par
        // l'utilisateur
        String errorMessage = "La méthode de paiement spécifiée n'est pas acceptée.";
        String providedMethod = "Bitcoin";

        // WHEN : Création d'une instance de InvalidPaymentMethodException avec le
        // constructeur à deux arguments (message et providedPaymentMethod)
        InvalidPaymentMethodException exception = new InvalidPaymentMethodException(errorMessage, providedMethod);

        // THEN : Vérification que le message et la méthode de paiement fournie sont
        // correctement définis
        assertEquals(errorMessage, exception.getMessage(), "Le message de l'exception doit correspondre.");
        assertNull(exception.getCause(), "La cause de l'exception devrait être nulle.");
        assertEquals(providedMethod, exception.getProvidedPaymentMethod(),
                "La méthode de paiement fournie doit correspondre.");
    }

    // Test pour vérifier la création de l'exception avec un message, une cause et
    // la méthode de paiement fournie
    @Test
    void invalidPaymentMethodException_shouldBeCreatedWithAllInformation() {
        // GIVEN : Un message d'erreur, une exception cause et la méthode de paiement
        // fournie
        String errorMessage = "Le traitement du paiement a échoué en raison d'une méthode invalide.";
        Throwable causeException = new SecurityException("Clé API invalide pour la méthode de paiement.");
        String providedMethod = "PayPal";

        // WHEN : Création d'une instance de InvalidPaymentMethodException avec le
        // constructeur à quatre arguments
        InvalidPaymentMethodException exception = new InvalidPaymentMethodException(errorMessage, causeException,
                providedMethod);

        // THEN : Vérification que tous les attributs sont correctement définis
        assertEquals(errorMessage, exception.getMessage(), "Le message de l'exception doit correspondre.");
        assertEquals(causeException, exception.getCause(), "La cause de l'exception doit correspondre.");
        assertEquals(providedMethod, exception.getProvidedPaymentMethod(),
                "La méthode de paiement fournie doit correspondre.");
    }

    // Test pour vérifier l'annotation @ResponseStatus sur la classe
    @Test
    void invalidPaymentMethodException_shouldHaveBadRequestStatus() {
        // GIVEN : La classe InvalidPaymentMethodException est annotée avec
        // @ResponseStatus

        // WHEN : Récupération de l'annotation ResponseStatus de la classe
        ResponseStatus responseStatus = InvalidPaymentMethodException.class.getAnnotation(ResponseStatus.class);

        // THEN : Vérification que l'annotation est présente et que sa valeur est
        // HttpStatus.BAD_REQUEST
        assertEquals(HttpStatus.BAD_REQUEST, responseStatus.value(),
                "L'exception devrait avoir le statut HTTP BAD_REQUEST.");
    }

    // Test pour vérifier la récupération de la méthode de paiement fournie via le
    // getter
    @Test
    void getProvidedPaymentMethod_shouldReturnCorrectProvidedMethod() {
        // GIVEN : Création d'une exception avec une méthode de paiement fournie
        // spécifique
        String providedMethod = "Visa";
        InvalidPaymentMethodException exception = new InvalidPaymentMethodException("Méthode de paiement non valide.",
                providedMethod);

        // WHEN : Appel de la méthode getProvidedPaymentMethod()
        String retrievedMethod = exception.getProvidedPaymentMethod();

        // THEN : Vérification que la méthode retourne la méthode de paiement fournie
        // correcte
        assertEquals(providedMethod, retrievedMethod,
                "getProvidedPaymentMethod() devrait retourner la méthode de paiement fournie correcte.");
    }
}