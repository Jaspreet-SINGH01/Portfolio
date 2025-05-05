package com.videoflix.subscriptions_microservice.exceptions;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class UserNotFoundExceptionTest {

    // Test pour vérifier la création de l'exception avec un message uniquement
    @Test
    void userNotFoundException_shouldBeCreatedWithMessageOnly() {
        // GIVEN : Un message indiquant que l'utilisateur n'a pas été trouvé
        String message = "Utilisateur non trouvé.";

        // WHEN : Création d'une instance de UserNotFoundException avec le constructeur
        // à un seul argument
        UserNotFoundException exception = new UserNotFoundException(message);

        // THEN : Vérification que le message est correctement défini et que la cause
        // est nulle
        assertEquals(message, exception.getMessage(), "Le message de l'exception doit correspondre.");
        assertNull(exception.getCause(), "La cause de l'exception devrait être nulle.");
    }

    // Test pour vérifier la création de l'exception avec un message et une cause
    @Test
    void userNotFoundException_shouldBeCreatedWithMessageAndCause() {
        // GIVEN : Un message indiquant que l'utilisateur n'a pas été trouvé et une
        // exception cause
        String message = "Erreur lors de la récupération de l'utilisateur.";
        Throwable causeException = new RuntimeException(
                "L'utilisateur avec l'ID spécifié n'existe pas dans la base de données.");

        // WHEN : Création d'une instance de UserNotFoundException avec le constructeur
        // à deux arguments
        UserNotFoundException exception = new UserNotFoundException(message, causeException);

        // THEN : Vérification que le message et la cause sont correctement définis
        assertEquals(message, exception.getMessage(), "Le message de l'exception doit correspondre.");
        assertEquals(causeException, exception.getCause(), "La cause de l'exception doit correspondre.");
    }

    // Test pour vérifier l'annotation @ResponseStatus sur la classe
    @Test
    void userNotFoundException_shouldHaveNotFoundStatus() {
        // GIVEN : La classe UserNotFoundException est annotée avec @ResponseStatus

        // WHEN : Récupération de l'annotation ResponseStatus de la classe
        ResponseStatus responseStatus = UserNotFoundException.class.getAnnotation(ResponseStatus.class);

        // THEN : Vérification que l'annotation est présente et que sa valeur est
        // HttpStatus.NOT_FOUND
        assertEquals(HttpStatus.NOT_FOUND, responseStatus.value(),
                "L'exception devrait avoir le statut HTTP NOT_FOUND.");
    }
}