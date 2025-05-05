package com.videoflix.subscriptions_microservice.exceptions;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CommunicationExceptionTest {

    // Test pour vérifier la création de l'exception avec un message uniquement
    @Test
    void communicationException_shouldBeCreatedWithMessageOnly() {
        // GIVEN : Un message d'erreur
        String errorMessage = "Erreur de communication avec le service externe.";

        // WHEN : Création d'une instance de CommunicationException avec le constructeur
        // à un seul argument
        CommunicationException exception = new CommunicationException(errorMessage);

        // THEN : Vérification que le message est correctement défini et que les autres
        // attributs sont initialisés par défaut
        assertEquals(errorMessage, exception.getMessage(), "Le message de l'exception doit correspondre.");
        assertNull(exception.getCause(), "La cause de l'exception devrait être nulle.");
        assertEquals("", exception.getRemoteServiceUrl(), "L'URL du service distant devrait être une chaîne vide.");
        assertEquals("", exception.getMethod(), "La méthode HTTP devrait être une chaîne vide.");
    }

    // Test pour vérifier la création de l'exception avec un message et une cause
    @Test
    void communicationException_shouldBeCreatedWithMessageAndCause() {
        // GIVEN : Un message d'erreur et une exception cause
        String errorMessage = "Erreur lors de la tentative de connexion.";
        Throwable causeException = new RuntimeException("Problème de réseau.");

        // WHEN : Création d'une instance de CommunicationException avec le constructeur
        // à deux arguments
        CommunicationException exception = new CommunicationException(errorMessage, causeException);

        // THEN : Vérification que le message et la cause sont correctement définis et
        // que les autres attributs sont initialisés par défaut
        assertEquals(errorMessage, exception.getMessage(), "Le message de l'exception doit correspondre.");
        assertEquals(causeException, exception.getCause(), "La cause de l'exception doit correspondre.");
        assertEquals("", exception.getRemoteServiceUrl(), "L'URL du service distant devrait être une chaîne vide.");
        assertEquals("", exception.getMethod(), "La méthode HTTP devrait être une chaîne vide.");
    }

    // Test pour vérifier la création de l'exception avec un message, l'URL du
    // service distant et la méthode HTTP
    @Test
    void communicationException_shouldBeCreatedWithDetailedInformation() {
        // GIVEN : Un message d'erreur, une URL de service distant et une méthode HTTP
        String errorMessage = "Le service de paiement est indisponible.";
        String remoteUrl = "http://payment-service/api/process";
        String httpMethod = "POST";

        // WHEN : Création d'une instance de CommunicationException avec le constructeur
        // à trois arguments
        CommunicationException exception = new CommunicationException(errorMessage, remoteUrl, httpMethod);

        // THEN : Vérification que tous les attributs sont correctement définis
        assertEquals(errorMessage, exception.getMessage(), "Le message de l'exception doit correspondre.");
        assertNull(exception.getCause(), "La cause de l'exception devrait être nulle.");
        assertEquals(remoteUrl, exception.getRemoteServiceUrl(), "L'URL du service distant doit correspondre.");
        assertEquals(httpMethod, exception.getMethod(), "La méthode HTTP doit correspondre.");
    }

    // Test pour vérifier la création de l'exception avec un message, une cause,
    // l'URL du service distant et la méthode HTTP
    @Test
    void communicationException_shouldBeCreatedWithAllInformation() {
        // GIVEN : Un message d'erreur, une exception cause, une URL de service distant
        // et une méthode HTTP
        String errorMessage = "Échec de la communication lors de la création de l'abonnement.";
        Throwable causeException = new java.net.ConnectException("Connexion refusée.");
        String remoteUrl = "http://user-service/api/users";
        String httpMethod = "POST";

        // WHEN : Création d'une instance de CommunicationException avec le constructeur
        // à quatre arguments
        CommunicationException exception = new CommunicationException(errorMessage, causeException, remoteUrl,
                httpMethod);

        // THEN : Vérification que tous les attributs sont correctement définis
        assertEquals(errorMessage, exception.getMessage(), "Le message de l'exception doit correspondre.");
        assertEquals(causeException, exception.getCause(), "La cause de l'exception doit correspondre.");
        assertEquals(remoteUrl, exception.getRemoteServiceUrl(), "L'URL du service distant doit correspondre.");
        assertEquals(httpMethod, exception.getMethod(), "La méthode HTTP doit correspondre.");
    }

    // Test pour vérifier l'annotation @ResponseStatus sur la classe
    @Test
    void communicationException_shouldHaveServiceUnavailableStatus() {
        // GIVEN : La classe CommunicationException est annotée avec @ResponseStatus

        // WHEN : Récupération de l'annotation ResponseStatus de la classe
        ResponseStatus responseStatus = CommunicationException.class.getAnnotation(ResponseStatus.class);

        // THEN : Vérification que l'annotation est présente et que sa valeur est
        // HttpStatus.SERVICE_UNAVAILABLE
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, responseStatus.value(),
                "L'exception devrait avoir le statut HTTP SERVICE_UNAVAILABLE.");
    }

    // Test pour vérifier la récupération de l'URL du service distant via le getter
    @Test
    void getRemoteServiceUrl_shouldReturnCorrectUrl() {
        // GIVEN : Création d'une exception avec une URL de service distant spécifique
        String remoteUrl = "https://external.api/data";
        CommunicationException exception = new CommunicationException("Test message", remoteUrl, "GET");

        // WHEN : Appel de la méthode getRemoteServiceUrl()
        String retrievedUrl = exception.getRemoteServiceUrl();

        // THEN : Vérification que la méthode retourne l'URL correcte
        assertEquals(remoteUrl, retrievedUrl, "getRemoteServiceUrl() devrait retourner l'URL correcte.");
    }

    // Test pour vérifier la récupération de la méthode HTTP via le getter
    @Test
    void getMethod_shouldReturnCorrectHttpMethod() {
        // GIVEN : Création d'une exception avec une méthode HTTP spécifique
        String httpMethod = "PUT";
        CommunicationException exception = new CommunicationException("Test message", "http://example.com", httpMethod);

        // WHEN : Appel de la méthode getMethod()
        String retrievedMethod = exception.getMethod();

        // THEN : Vérification que la méthode retourne la méthode HTTP correcte
        assertEquals(httpMethod, retrievedMethod, "getMethod() devrait retourner la méthode HTTP correcte.");
    }
}