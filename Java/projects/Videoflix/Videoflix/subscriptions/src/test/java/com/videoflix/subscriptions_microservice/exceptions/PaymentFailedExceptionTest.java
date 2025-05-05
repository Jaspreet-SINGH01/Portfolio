package com.videoflix.subscriptions_microservice.exceptions;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PaymentFailedExceptionTest {

    // Test pour vérifier la création de l'exception avec un message uniquement
    @Test
    void paymentFailedException_shouldBeCreatedWithMessageOnly() {
        // GIVEN : Un message d'erreur générique indiquant un échec de paiement
        String errorMessage = "Le paiement a échoué.";

        // WHEN : Création d'une instance de PaymentFailedException avec le constructeur
        // à un seul argument
        PaymentFailedException exception = new PaymentFailedException(errorMessage);

        // THEN : Vérification que le message est correctement défini et que les
        // attributs spécifiques au paiement sont initialisés par défaut
        assertEquals(errorMessage, exception.getMessage(), "Le message de l'exception doit correspondre.");
        assertNull(exception.getCause(), "La cause de l'exception devrait être nulle.");
        assertEquals("", exception.getPaymentErrorCode(), "Le code d'erreur de paiement devrait être une chaîne vide.");
        assertEquals("", exception.getPaymentErrorMessage(),
                "Le message d'erreur de paiement devrait être une chaîne vide.");
    }

    // Test pour vérifier la création de l'exception avec un message et une cause
    @Test
    void paymentFailedException_shouldBeCreatedWithMessageAndCause() {
        // GIVEN : Un message d'erreur et une exception cause
        String errorMessage = "Une erreur s'est produite lors du traitement du paiement.";
        Throwable causeException = new RuntimeException("Erreur de connexion au processeur de paiement.");

        // WHEN : Création d'une instance de PaymentFailedException avec le constructeur
        // à deux arguments
        PaymentFailedException exception = new PaymentFailedException(errorMessage, causeException);

        // THEN : Vérification que le message et la cause sont correctement définis et
        // que les attributs spécifiques au paiement sont initialisés par défaut
        assertEquals(errorMessage, exception.getMessage(), "Le message de l'exception doit correspondre.");
        assertEquals(causeException, exception.getCause(), "La cause de l'exception doit correspondre.");
        assertEquals("", exception.getPaymentErrorCode(), "Le code d'erreur de paiement devrait être une chaîne vide.");
        assertEquals("", exception.getPaymentErrorMessage(),
                "Le message d'erreur de paiement devrait être une chaîne vide.");
    }

    // Test pour vérifier la création de l'exception avec un message, un code
    // d'erreur et un message d'erreur de paiement
    @Test
    void paymentFailedException_shouldBeCreatedWithPaymentDetails() {
        // GIVEN : Un message d'erreur générique et des détails spécifiques de l'échec
        // du paiement
        String errorMessage = "Le paiement n'a pas pu être effectué.";
        String errorCode = "card_declined";
        String detailedMessage = "Votre carte a été refusée par votre banque.";

        // WHEN : Création d'une instance de PaymentFailedException avec le constructeur
        // à trois arguments
        PaymentFailedException exception = new PaymentFailedException(errorMessage, errorCode, detailedMessage);

        // THEN : Vérification que le message et les détails spécifiques du paiement
        // sont correctement définis
        assertEquals(errorMessage, exception.getMessage(), "Le message de l'exception doit correspondre.");
        assertNull(exception.getCause(), "La cause de l'exception devrait être nulle.");
        assertEquals(errorCode, exception.getPaymentErrorCode(), "Le code d'erreur de paiement doit correspondre.");
        assertEquals(detailedMessage, exception.getPaymentErrorMessage(),
                "Le message d'erreur de paiement doit correspondre.");
    }

    // Test pour vérifier la création de l'exception avec toutes les informations
    // (message, cause, code d'erreur, message d'erreur)
    @Test
    void paymentFailedException_shouldBeCreatedWithAllInformation() {
        // GIVEN : Un message d'erreur, une cause, un code d'erreur et un message
        // d'erreur détaillé
        String errorMessage = "La tentative de paiement a échoué en raison d'une erreur externe.";
        Throwable causeException = new RuntimeException("Erreur lors de l'appel à l'API de paiement.");
        String errorCode = "invalid_request";
        String detailedMessage = "Paramètre de requête non valide.";

        // WHEN : Création d'une instance de PaymentFailedException avec le constructeur
        // à quatre arguments
        PaymentFailedException exception = new PaymentFailedException(errorMessage, causeException, errorCode,
                detailedMessage);

        // THEN : Vérification que toutes les informations sont correctement définies
        assertEquals(errorMessage, exception.getMessage(), "Le message de l'exception doit correspondre.");
        assertEquals(causeException, exception.getCause(), "La cause de l'exception doit correspondre.");
        assertEquals(errorCode, exception.getPaymentErrorCode(), "Le code d'erreur de paiement doit correspondre.");
        assertEquals(detailedMessage, exception.getPaymentErrorMessage(),
                "Le message d'erreur de paiement doit correspondre.");
    }

    // Test pour vérifier l'annotation @ResponseStatus sur la classe
    @Test
    void paymentFailedException_shouldHavePaymentRequiredStatus() {
        // GIVEN : La classe PaymentFailedException est annotée avec @ResponseStatus

        // WHEN : Récupération de l'annotation ResponseStatus de la classe
        ResponseStatus responseStatus = PaymentFailedException.class.getAnnotation(ResponseStatus.class);

        // THEN : Vérification que l'annotation est présente et que sa valeur est
        // HttpStatus.PAYMENT_REQUIRED
        assertEquals(HttpStatus.PAYMENT_REQUIRED, responseStatus.value(),
                "L'exception devrait avoir le statut HTTP PAYMENT_REQUIRED.");
    }

    // Test pour vérifier la récupération du code d'erreur de paiement via le getter
    @Test
    void getPaymentErrorCode_shouldReturnCorrectErrorCode() {
        // GIVEN : Création d'une exception avec un code d'erreur de paiement spécifique
        String errorCode = "expired_card";
        PaymentFailedException exception = new PaymentFailedException("Paiement refusé.", errorCode,
                "La carte a expiré.");

        // WHEN : Appel de la méthode getPaymentErrorCode()
        String retrievedErrorCode = exception.getPaymentErrorCode();

        // THEN : Vérification que la méthode retourne le code d'erreur correct
        assertEquals(errorCode, retrievedErrorCode,
                "getPaymentErrorCode() devrait retourner le code d'erreur correct.");
    }

    // Test pour vérifier la récupération du message d'erreur de paiement via le
    // getter
    @Test
    void getPaymentErrorMessage_shouldReturnCorrectErrorMessage() {
        // GIVEN : Création d'une exception avec un message d'erreur de paiement
        // spécifique
        String errorMessage = "Le montant dépasse la limite autorisée.";
        PaymentFailedException exception = new PaymentFailedException("Paiement refusé.", "amount_exceeded",
                errorMessage);

        // WHEN : Appel de la méthode getPaymentErrorMessage()
        String retrievedErrorMessage = exception.getPaymentErrorMessage();

        // THEN : Vérification que la méthode retourne le message d'erreur correct
        assertEquals(errorMessage, retrievedErrorMessage,
                "getPaymentErrorMessage() devrait retourner le message d'erreur correct.");
    }
}