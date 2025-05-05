package com.videoflix.subscriptions_microservice.exceptions;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class UserNotAuthorizedExceptionTest {

    // Test pour vérifier la création de l'exception avec un message uniquement
    @Test
    void userNotAuthorizedException_shouldBeCreatedWithMessageOnly() {
        // GIVEN : Un message indiquant que l'utilisateur n'est pas autorisé
        String message = "L'utilisateur n'est pas autorisé à effectuer cette action.";

        // WHEN : Création d'une instance de UserNotAuthorizedException avec le
        // constructeur à un seul argument
        UserNotAuthorizedException exception = new UserNotAuthorizedException(message);

        // THEN : Vérification que le message est correctement défini et que les autres
        // attributs sont nuls ou vides
        assertEquals(message, exception.getMessage(), "Le message de l'exception doit correspondre.");
        assertNull(exception.getCause(), "La cause de l'exception devrait être nulle.");
        assertNull(exception.getUserId(), "L'ID de l'utilisateur devrait être null.");
        assertNull(exception.getResourceId(), "L'ID de la ressource devrait être null.");
        assertEquals("", exception.getOperation(), "L'opération devrait être une chaîne vide.");
    }

    // Test pour vérifier la création de l'exception avec un message et une cause
    @Test
    void userNotAuthorizedException_shouldBeCreatedWithMessageAndCause() {
        // GIVEN : Un message indiquant que l'utilisateur n'est pas autorisé et une
        // exception cause
        String message = "Autorisation refusée.";
        Throwable causeException = new SecurityException("Permissions insuffisantes.");

        // WHEN : Création d'une instance de UserNotAuthorizedException avec le
        // constructeur à deux arguments
        UserNotAuthorizedException exception = new UserNotAuthorizedException(message, causeException);

        // THEN : Vérification que le message et la cause sont correctement définis et
        // que les autres attributs sont nuls ou vides
        assertEquals(message, exception.getMessage(), "Le message de l'exception doit correspondre.");
        assertEquals(causeException, exception.getCause(), "La cause de l'exception doit correspondre.");
        assertNull(exception.getUserId(), "L'ID de l'utilisateur devrait être null.");
        assertNull(exception.getResourceId(), "L'ID de la ressource devrait être null.");
        assertEquals("", exception.getOperation(), "L'opération devrait être une chaîne vide.");
    }

    // Test pour vérifier la création de l'exception avec toutes les informations
    // (message, userId, resourceId, operation)
    @Test
    void userNotAuthorizedException_shouldBeCreatedWithAllInformation() {
        // GIVEN : Un message, l'ID de l'utilisateur, l'ID de la ressource et
        // l'opération tentée
        String message = "L'utilisateur 123 n'est pas autorisé à modifier l'abonnement 456.";
        Long userId = 123L;
        Long resourceId = 456L;
        String operation = "UPDATE";

        // WHEN : Création d'une instance de UserNotAuthorizedException avec le
        // constructeur à quatre arguments
        UserNotAuthorizedException exception = new UserNotAuthorizedException(message, userId, resourceId, operation);

        // THEN : Vérification que tous les attributs sont correctement définis
        assertEquals(message, exception.getMessage(), "Le message de l'exception doit correspondre.");
        assertNull(exception.getCause(), "La cause de l'exception devrait être nulle.");
        assertEquals(userId, exception.getUserId(), "L'ID de l'utilisateur doit correspondre.");
        assertEquals(resourceId, exception.getResourceId(), "L'ID de la ressource doit correspondre.");
        assertEquals(operation, exception.getOperation(), "L'opération doit correspondre.");
    }

    // Test pour vérifier la création de l'exception avec toutes les informations
    // incluant la cause
    @Test
    void userNotAuthorizedException_shouldBeCreatedWithCauseAndAllInformation() {
        // GIVEN : Un message, une cause, l'ID de l'utilisateur, l'ID de la ressource et
        // l'opération tentée
        String message = "Erreur lors de la vérification des autorisations.";
        Throwable causeException = new SecurityException("Rôle utilisateur insuffisant.");
        Long userId = 789L;
        Long resourceId = 101L;
        String operation = "DELETE";

        // WHEN : Création d'une instance de UserNotAuthorizedException avec le
        // constructeur à cinq arguments
        UserNotAuthorizedException exception = new UserNotAuthorizedException(message, causeException, userId,
                resourceId, operation);

        // THEN : Vérification que tous les attributs sont correctement définis
        assertEquals(message, exception.getMessage(), "Le message de l'exception doit correspondre.");
        assertEquals(causeException, exception.getCause(), "La cause de l'exception doit correspondre.");
        assertEquals(userId, exception.getUserId(), "L'ID de l'utilisateur doit correspondre.");
        assertEquals(resourceId, exception.getResourceId(), "L'ID de la ressource doit correspondre.");
        assertEquals(operation, exception.getOperation(), "L'opération doit correspondre.");
    }

    // Test pour vérifier l'annotation @ResponseStatus sur la classe
    @Test
    void userNotAuthorizedException_shouldHaveForbiddenStatus() {
        // GIVEN : La classe UserNotAuthorizedException est annotée avec @ResponseStatus

        // WHEN : Récupération de l'annotation ResponseStatus de la classe
        ResponseStatus responseStatus = UserNotAuthorizedException.class.getAnnotation(ResponseStatus.class);

        // THEN : Vérification que l'annotation est présente et que sa valeur est
        // HttpStatus.FORBIDDEN
        assertEquals(HttpStatus.FORBIDDEN, responseStatus.value(),
                "L'exception devrait avoir le statut HTTP FORBIDDEN.");
    }

    // Test pour vérifier la récupération de l'ID de l'utilisateur via le getter
    @Test
    void getUserId_shouldReturnCorrectUserId() {
        // GIVEN : Création d'une exception avec un ID d'utilisateur spécifique
        Long userId = 111L;
        UserNotAuthorizedException exception = new UserNotAuthorizedException("Non autorisé.", userId, 222L, "VIEW");

        // WHEN : Appel de la méthode getUserId()
        Long retrievedUserId = exception.getUserId();

        // THEN : Vérification que la méthode retourne l'ID de l'utilisateur correct
        assertEquals(userId, retrievedUserId, "getUserId() devrait retourner l'ID de l'utilisateur correct.");
    }

    // Test pour vérifier la récupération de l'ID de la ressource via le getter
    @Test
    void getResourceId_shouldReturnCorrectResourceId() {
        // GIVEN : Création d'une exception avec un ID de ressource spécifique
        Long resourceId = 333L;
        UserNotAuthorizedException exception = new UserNotAuthorizedException("Non autorisé.", 444L, resourceId,
                "ACTIVATE");

        // WHEN : Appel de la méthode getResourceId()
        Long retrievedResourceId = exception.getResourceId();

        // THEN : Vérification que la méthode retourne l'ID de la ressource correct
        assertEquals(resourceId, retrievedResourceId,
                "getResourceId() devrait retourner l'ID de la ressource correct.");
    }

    // Test pour vérifier la récupération de l'opération via le getter
    @Test
    void getOperation_shouldReturnCorrectOperation() {
        // GIVEN : Création d'une exception avec une opération spécifique
        String operation = "DEACTIVATE";
        UserNotAuthorizedException exception = new UserNotAuthorizedException("Non autorisé.", 555L, 666L, operation);

        // WHEN : Appel de la méthode getOperation()
        String retrievedOperation = exception.getOperation();

        // THEN : Vérification que la méthode retourne l'opération correcte
        assertEquals(operation, retrievedOperation, "getOperation() devrait retourner l'opération correcte.");
    }
}