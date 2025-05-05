package com.videoflix.subscriptions_microservice.exceptions;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class UserServiceUnavailableExceptionTest {

    // Test pour vérifier la création de l'exception avec un message uniquement
    @Test
    void userServiceUnavailableException_shouldBeCreatedWithMessageOnly() {
        // GIVEN : Un message indiquant que le service utilisateur est indisponible
        String message = "Le service utilisateur est temporairement indisponible.";

        // WHEN : Création d'une instance de UserServiceUnavailableException avec le
        // constructeur à un seul argument
        UserServiceUnavailableException exception = new UserServiceUnavailableException(message);

        // THEN : Vérification que le message est correctement défini et que les
        // attributs hérités sont initialisés par défaut
        assertEquals(message, exception.getMessage(), "Le message de l'exception doit correspondre.");
        assertNull(exception.getCause(), "La cause de l'exception devrait être nulle.");
        assertEquals("", exception.getRemoteServiceUrl(), "L'URL du service distant devrait être une chaîne vide.");
        assertEquals("", exception.getMethod(), "La méthode HTTP devrait être une chaîne vide.");
    }

    // Test pour vérifier la création de l'exception avec un message et une cause
    @Test
    void userServiceUnavailableException_shouldBeCreatedWithMessageAndCause() {
        // GIVEN : Un message et une exception cause
        String message = "Erreur lors de la connexion au service utilisateur.";
        Throwable causeException = new java.net.ConnectException("Connexion refusée au service utilisateur.");

        // WHEN : Création d'une instance de UserServiceUnavailableException avec le
        // constructeur à deux arguments
        UserServiceUnavailableException exception = new UserServiceUnavailableException(message, causeException);

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
    void userServiceUnavailableException_shouldBeCreatedWithRemoteServiceInfo() {
        // GIVEN : Un message, une URL de service distant et une méthode HTTP
        String message = "Le service utilisateur n'est pas accessible pour le moment.";
        String remoteUrl = "https://user-service/api/users";
        String method = "GET";

        // WHEN : Création d'une instance de UserServiceUnavailableException avec le
        // constructeur à trois arguments
        UserServiceUnavailableException exception = new UserServiceUnavailableException(message, remoteUrl, method);

        // THEN : Vérification que tous les attributs sont correctement définis
        assertEquals(message, exception.getMessage(), "Le message de l'exception doit correspondre.");
        assertNull(exception.getCause(), "La cause de l'exception devrait être nulle.");
        assertEquals(remoteUrl, exception.getRemoteServiceUrl(), "L'URL du service distant doit correspondre.");
        assertEquals(method, exception.getMethod(), "La méthode HTTP doit correspondre.");
    }

    // Test pour vérifier la création de l'exception avec toutes les informations
    // incluant la cause
    @Test
    void userServiceUnavailableException_shouldBeCreatedWithCauseAndRemoteServiceInfo() {
        // GIVEN : Un message, une cause, une URL de service distant et une méthode HTTP
        String message = "Erreur de communication avec le service utilisateur lors de la récupération des détails.";
        Throwable causeException = new java.net.SocketTimeoutException(
                "Délai d'attente expiré lors de la communication avec le service utilisateur.");
        String remoteUrl = "https://user-service/api/users/123";
        String method = "GET";

        // WHEN : Création d'une instance de UserServiceUnavailableException avec le
        // constructeur à quatre arguments
        UserServiceUnavailableException exception = new UserServiceUnavailableException(message, causeException,
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
    void userServiceUnavailableException_shouldHaveServiceUnavailableStatus() {
        // GIVEN : La classe UserServiceUnavailableException hérite de
        // CommunicationException qui est annotée avec
        // @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)

        // WHEN : Récupération de l'annotation ResponseStatus de la classe
        // UserServiceUnavailableException
        ResponseStatus responseStatus = UserServiceUnavailableException.class.getAnnotation(ResponseStatus.class);

        // THEN : Vérification que l'annotation est présente et que sa valeur est
        // HttpStatus.SERVICE_UNAVAILABLE
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, responseStatus.value(),
                "L'exception devrait avoir le statut HTTP SERVICE_UNAVAILABLE.");
    }
}