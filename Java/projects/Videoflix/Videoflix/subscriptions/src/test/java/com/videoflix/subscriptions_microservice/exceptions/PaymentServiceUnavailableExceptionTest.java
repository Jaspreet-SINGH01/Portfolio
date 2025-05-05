package com.videoflix.subscriptions_microservice.exceptions;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PaymentServiceUnavailableExceptionTest {

    // Test pour vérifier la création de l'exception avec un message uniquement
    @Test
    void paymentServiceUnavailableException_shouldBeCreatedWithMessageOnly() {
        // GIVEN : Un message indiquant que le service de paiement est indisponible
        String message = "Le service de paiement est temporairement indisponible.";

        // WHEN : Création d'une instance de PaymentServiceUnavailableException avec le
        // constructeur à un seul argument
        PaymentServiceUnavailableException exception = new PaymentServiceUnavailableException(message);

        // THEN : Vérification que le message est correctement défini et que les
        // attributs hérités sont initialisés par défaut
        assertEquals(message, exception.getMessage(), "Le message de l'exception doit correspondre.");
        assertNull(exception.getCause(), "La cause de l'exception devrait être nulle.");
        assertEquals("", exception.getRemoteServiceUrl(), "L'URL du service distant devrait être une chaîne vide.");
        assertEquals("", exception.getMethod(), "La méthode HTTP devrait être une chaîne vide.");
    }

    // Test pour vérifier la création de l'exception avec un message et une cause
    @Test
    void paymentServiceUnavailableException_shouldBeCreatedWithMessageAndCause() {
        // GIVEN : Un message et une exception cause
        String message = "Erreur lors de la connexion au service de paiement.";
        Throwable causeException = new java.net.ConnectException("Connexion refusée au service de paiement.");

        // WHEN : Création d'une instance de PaymentServiceUnavailableException avec le
        // constructeur à deux arguments
        PaymentServiceUnavailableException exception = new PaymentServiceUnavailableException(message, causeException);

        // THEN : Vérification que le message et la cause sont correctement définis et
        // que les attributs hérités sont initialisés par défaut
        assertEquals(message, exception.getMessage(), "Le message de l'exception doit correspondre.");
        assertEquals(causeException, exception.getCause(), "La cause de l'exception doit correspondre.");
        assertEquals("", exception.getRemoteServiceUrl(), "L'URL du service distant devrait être une chaîne vide.");
        assertEquals("", exception.getMethod(), "La méthode HTTP devrait être une chaîne vide.");
    }

    // Test pour vérifier la création de l'exception avec toutes les informations
    // (message, URL, méthode)
    @Test
    void paymentServiceUnavailableException_shouldBeCreatedWithRemoteServiceInfo() {
        // GIVEN : Un message, une URL de service distant et une méthode HTTP
        String message = "Le service de paiement n'est pas accessible pour le moment.";
        String remoteUrl = "https://payment-gateway/api";
        String method = "POST";

        // WHEN : Création d'une instance de PaymentServiceUnavailableException avec le
        // constructeur à trois arguments
        PaymentServiceUnavailableException exception = new PaymentServiceUnavailableException(message, remoteUrl,
                method);

        // THEN : Vérification que tous les attributs sont correctement définis
        assertEquals(message, exception.getMessage(), "Le message de l'exception doit correspondre.");
        assertNull(exception.getCause(), "La cause de l'exception devrait être nulle.");
        assertEquals(remoteUrl, exception.getRemoteServiceUrl(), "L'URL du service distant doit correspondre.");
        assertEquals(method, exception.getMethod(), "La méthode HTTP doit correspondre.");
    }

    // Test pour vérifier la création de l'exception avec toutes les informations
    // incluant la cause
    @Test
    void paymentServiceUnavailableException_shouldBeCreatedWithCauseAndRemoteServiceInfo() {
        // GIVEN : Un message, une cause, une URL de service distant et une méthode HTTP
        String message = "Erreur de communication avec le service de paiement.";
        Throwable causeException = new java.net.SocketTimeoutException(
                "Délai d'attente expiré lors de la communication avec le paiement.");
        String remoteUrl = "https://payment-processor/charge";
        String method = "PUT";

        // WHEN : Création d'une instance de PaymentServiceUnavailableException avec le
        // constructeur à quatre arguments
        PaymentServiceUnavailableException exception = new PaymentServiceUnavailableException(message, causeException,
                remoteUrl, method);

        // THEN : Vérification que tous les attributs sont correctement définis
        assertEquals(message, exception.getMessage(), "Le message de l'exception doit correspondre.");
        assertEquals(causeException, exception.getCause(), "La cause de l'exception doit correspondre.");
        assertEquals(remoteUrl, exception.getRemoteServiceUrl(), "L'URL du service distant doit correspondre.");
        assertEquals(method, exception.getMethod(), "La méthode HTTP doit correspondre.");
    }

    // Test pour vérifier l'annotation @ResponseStatus héritée de
    // CommunicationException
    @Test
    void paymentServiceUnavailableException_shouldHaveServiceUnavailableStatus() {
        // GIVEN : La classe PaymentServiceUnavailableException hérite de
        // CommunicationException qui est annotée avec
        // @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)

        // WHEN : Récupération de l'annotation ResponseStatus de la classe
        // PaymentServiceUnavailableException
        ResponseStatus responseStatus = PaymentServiceUnavailableException.class.getAnnotation(ResponseStatus.class);

        // THEN : Vérification que l'annotation est présente et que sa valeur est
        // HttpStatus.SERVICE_UNAVAILABLE
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, responseStatus.value(),
                "L'exception devrait avoir le statut HTTP SERVICE_UNAVAILABLE.");
    }
}