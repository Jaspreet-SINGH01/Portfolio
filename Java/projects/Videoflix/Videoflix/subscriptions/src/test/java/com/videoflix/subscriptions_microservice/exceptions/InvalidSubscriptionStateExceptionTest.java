package com.videoflix.subscriptions_microservice.exceptions;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class InvalidSubscriptionStateExceptionTest {

    // Test pour vérifier la création de l'exception avec un message uniquement
    @Test
    void invalidSubscriptionStateException_shouldBeCreatedWithMessageOnly() {
        // GIVEN : Un message d'erreur indiquant un état d'abonnement invalide pour une
        // opération
        String errorMessage = "L'opération n'est pas autorisée dans l'état actuel de l'abonnement.";

        // WHEN : Création d'une instance de InvalidSubscriptionStateException avec le
        // constructeur à un seul argument
        InvalidSubscriptionStateException exception = new InvalidSubscriptionStateException(errorMessage);

        // THEN : Vérification que le message est correctement défini et que les autres
        // attributs sont initialisés par défaut
        assertEquals(errorMessage, exception.getMessage(), "Le message de l'exception doit correspondre.");
        assertNull(exception.getCause(), "La cause de l'exception devrait être nulle.");
        assertEquals("", exception.getCurrentState(), "L'état actuel devrait être une chaîne vide.");
        assertEquals("", exception.getAttemptedOperation(), "L'opération tentée devrait être une chaîne vide.");
    }

    // Test pour vérifier la création de l'exception avec un message et une cause
    @Test
    void invalidSubscriptionStateException_shouldBeCreatedWithMessageAndCause() {
        // GIVEN : Un message d'erreur et une exception cause
        String errorMessage = "Erreur lors de la tentative de changement d'état de l'abonnement.";
        Throwable causeException = new IllegalStateException("L'abonnement est déjà annulé.");

        // WHEN : Création d'une instance de InvalidSubscriptionStateException avec le
        // constructeur à deux arguments
        InvalidSubscriptionStateException exception = new InvalidSubscriptionStateException(errorMessage,
                causeException);

        // THEN : Vérification que le message et la cause sont correctement définis et
        // que les autres attributs sont initialisés par défaut
        assertEquals(errorMessage, exception.getMessage(), "Le message de l'exception doit correspondre.");
        assertEquals(causeException, exception.getCause(), "La cause de l'exception doit correspondre.");
        assertEquals("", exception.getCurrentState(), "L'état actuel devrait être une chaîne vide.");
        assertEquals("", exception.getAttemptedOperation(), "L'opération tentée devrait être une chaîne vide.");
    }

    // Test pour vérifier la création de l'exception avec un message, l'état actuel
    // et l'opération tentée
    @Test
    void invalidSubscriptionStateException_shouldBeCreatedWithCurrentStateAndOperation() {
        // GIVEN : Un message d'erreur, l'état actuel de l'abonnement et l'opération
        // tentée
        String errorMessage = "Impossible d'annuler un abonnement déjà expiré.";
        String currentState = "EXPIRED";
        String attemptedOperation = "CANCEL";

        // WHEN : Création d'une instance de InvalidSubscriptionStateException avec le
        // constructeur à trois arguments
        InvalidSubscriptionStateException exception = new InvalidSubscriptionStateException(errorMessage, currentState,
                attemptedOperation);

        // THEN : Vérification que le message, l'état actuel et l'opération tentée sont
        // correctement définis
        assertEquals(errorMessage, exception.getMessage(), "Le message de l'exception doit correspondre.");
        assertNull(exception.getCause(), "La cause de l'exception devrait être nulle.");
        assertEquals(currentState, exception.getCurrentState(), "L'état actuel doit correspondre.");
        assertEquals(attemptedOperation, exception.getAttemptedOperation(), "L'opération tentée doit correspondre.");
    }

    // Test pour vérifier la création de l'exception avec un message, une cause,
    // l'état actuel et l'opération tentée
    @Test
    void invalidSubscriptionStateException_shouldBeCreatedWithMessageCauseStateAndOperation() {
        // GIVEN : Un message d'erreur, une cause, l'état actuel de l'abonnement et
        // l'opération tentée
        String errorMessage = "Erreur lors de la vérification de l'état de l'abonnement pour la reprise.";
        Throwable causeException = new IllegalStateException("La base de données d'état est inaccessible.");
        String currentState = "PENDING_CANCELLATION";
        String attemptedOperation = "RESUME";

        // WHEN : Création d'une instance de InvalidSubscriptionStateException avec le
        // constructeur à quatre arguments
        InvalidSubscriptionStateException exception = new InvalidSubscriptionStateException(errorMessage,
                causeException, currentState, attemptedOperation);

        // THEN : Vérification que le message, la cause, l'état actuel et l'opération
        // tentée sont correctement définis
        assertEquals(errorMessage, exception.getMessage(), "Le message de l'exception doit correspondre.");
        assertEquals(causeException, exception.getCause(), "La cause de l'exception doit correspondre.");
        assertEquals(currentState, exception.getCurrentState(), "L'état actuel doit correspondre.");
        assertEquals(attemptedOperation, exception.getAttemptedOperation(), "L'opération tentée doit correspondre.");
    }

    // Test pour vérifier l'annotation @ResponseStatus sur la classe
    @Test
    void invalidSubscriptionStateException_shouldHaveBadRequestStatus() {
        // GIVEN : La classe InvalidSubscriptionStateException est annotée avec
        // @ResponseStatus

        // WHEN : Récupération de l'annotation ResponseStatus de la classe
        ResponseStatus responseStatus = InvalidSubscriptionStateException.class.getAnnotation(ResponseStatus.class);

        // THEN : Vérification que l'annotation est présente et que sa valeur est
        // HttpStatus.BAD_REQUEST
        assertEquals(HttpStatus.BAD_REQUEST, responseStatus.value(),
                "L'exception devrait avoir le statut HTTP BAD_REQUEST.");
    }

    // Test pour vérifier la récupération de l'état actuel via le getter
    @Test
    void getCurrentState_shouldReturnCorrectCurrentState() {
        // GIVEN : Création d'une exception avec un état actuel spécifique
        String currentState = "ACTIVE";
        InvalidSubscriptionStateException exception = new InvalidSubscriptionStateException("Opération invalide.",
                currentState, "UPGRADE");

        // WHEN : Appel de la méthode getCurrentState()
        String retrievedState = exception.getCurrentState();

        // THEN : Vérification que la méthode retourne l'état actuel correct
        assertEquals(currentState, retrievedState, "getCurrentState() devrait retourner l'état actuel correct.");
    }

    // Test pour vérifier la récupération de l'opération tentée via le getter
    @Test
    void getAttemptedOperation_shouldReturnCorrectAttemptedOperation() {
        // GIVEN : Création d'une exception avec une opération tentée spécifique
        String attemptedOperation = "RENEW";
        InvalidSubscriptionStateException exception = new InvalidSubscriptionStateException("Opération invalide.",
                "EXPIRED", attemptedOperation);

        // WHEN : Appel de la méthode getAttemptedOperation()
        String retrievedOperation = exception.getAttemptedOperation();

        // THEN : Vérification que la méthode retourne l'opération tentée correcte
        assertEquals(attemptedOperation, retrievedOperation,
                "getAttemptedOperation() devrait retourner l'opération tentée correcte.");
    }
}