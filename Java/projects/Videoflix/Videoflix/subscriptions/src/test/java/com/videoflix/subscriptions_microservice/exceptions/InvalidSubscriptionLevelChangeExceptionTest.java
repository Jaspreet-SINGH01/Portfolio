package com.videoflix.subscriptions_microservice.exceptions;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class InvalidSubscriptionLevelChangeExceptionTest {

    // Test pour vérifier la création de l'exception avec un message uniquement
    @Test
    void invalidSubscriptionLevelChangeException_shouldBeCreatedWithMessageOnly() {
        // GIVEN : Un message d'erreur indiquant un changement de niveau d'abonnement
        // invalide
        String errorMessage = "Le changement de niveau d'abonnement n'est pas autorisé.";

        // WHEN : Création d'une instance de InvalidSubscriptionLevelChangeException
        // avec le constructeur à un seul argument
        InvalidSubscriptionLevelChangeException exception = new InvalidSubscriptionLevelChangeException(errorMessage);

        // THEN : Vérification que le message est correctement défini et que les autres
        // attributs sont initialisés par défaut
        assertEquals(errorMessage, exception.getMessage(), "Le message de l'exception doit correspondre.");
        assertNull(exception.getCause(), "La cause de l'exception devrait être nulle.");
        assertEquals("", exception.getCurrentLevel(), "Le niveau actuel devrait être une chaîne vide.");
        assertEquals("", exception.getRequestedLevel(), "Le niveau demandé devrait être une chaîne vide.");
        assertEquals("", exception.getReason(), "La raison du rejet devrait être une chaîne vide.");
    }

    // Test pour vérifier la création de l'exception avec un message et une cause
    @Test
    void invalidSubscriptionLevelChangeException_shouldBeCreatedWithMessageAndCause() {
        // GIVEN : Un message d'erreur et une exception cause
        String errorMessage = "Erreur lors de la tentative de changement de niveau.";
        Throwable causeException = new IllegalStateException("Le niveau actuel n'est pas valide.");

        // WHEN : Création d'une instance de InvalidSubscriptionLevelChangeException
        // avec le constructeur à deux arguments
        InvalidSubscriptionLevelChangeException exception = new InvalidSubscriptionLevelChangeException(errorMessage,
                causeException);

        // THEN : Vérification que le message et la cause sont correctement définis et
        // que les autres attributs sont initialisés par défaut
        assertEquals(errorMessage, exception.getMessage(), "Le message de l'exception doit correspondre.");
        assertEquals(causeException, exception.getCause(), "La cause de l'exception doit correspondre.");
        assertEquals("", exception.getCurrentLevel(), "Le niveau actuel devrait être une chaîne vide.");
        assertEquals("", exception.getRequestedLevel(), "Le niveau demandé devrait être une chaîne vide.");
        assertEquals("", exception.getReason(), "La raison du rejet devrait être une chaîne vide.");
    }

    // Test pour vérifier la création de l'exception avec un message, le niveau
    // actuel et le niveau demandé
    @Test
    void invalidSubscriptionLevelChangeException_shouldBeCreatedWithCurrentAndRequestedLevels() {
        // GIVEN : Un message d'erreur, le niveau actuel et le niveau demandé
        String errorMessage = "Le changement de BASIC à ULTRA n'est pas autorisé immédiatement.";
        String currentLevel = "BASIC";
        String requestedLevel = "ULTRA";

        // WHEN : Création d'une instance de InvalidSubscriptionLevelChangeException
        // avec le constructeur à trois arguments
        InvalidSubscriptionLevelChangeException exception = new InvalidSubscriptionLevelChangeException(errorMessage,
                currentLevel, requestedLevel);

        // THEN : Vérification que le message, le niveau actuel et le niveau demandé
        // sont correctement définis
        assertEquals(errorMessage, exception.getMessage(), "Le message de l'exception doit correspondre.");
        assertNull(exception.getCause(), "La cause de l'exception devrait être nulle.");
        assertEquals(currentLevel, exception.getCurrentLevel(), "Le niveau actuel doit correspondre.");
        assertEquals(requestedLevel, exception.getRequestedLevel(), "Le niveau demandé doit correspondre.");
        assertEquals("", exception.getReason(), "La raison du rejet devrait être une chaîne vide.");
    }

    // Test pour vérifier la création de l'exception avec un message, une cause, le
    // niveau actuel et le niveau demandé
    @Test
    void invalidSubscriptionLevelChangeException_shouldBeCreatedWithMessageCauseAndLevels() {
        // GIVEN : Un message d'erreur, une cause, le niveau actuel et le niveau demandé
        String errorMessage = "Erreur interne lors de la vérification du changement de niveau.";
        Throwable causeException = new RuntimeException("La base de données est temporairement indisponible.");
        String currentLevel = "PREMIUM";
        String requestedLevel = "BASIC";

        // WHEN : Création d'une instance de InvalidSubscriptionLevelChangeException
        // avec le constructeur à quatre arguments
        InvalidSubscriptionLevelChangeException exception = new InvalidSubscriptionLevelChangeException(errorMessage,
                causeException, currentLevel, requestedLevel);

        // THEN : Vérification que le message, la cause, le niveau actuel et le niveau
        // demandé sont correctement définis
        assertEquals(errorMessage, exception.getMessage(), "Le message de l'exception doit correspondre.");
        assertEquals(causeException, exception.getCause(), "La cause de l'exception doit correspondre.");
        assertEquals(currentLevel, exception.getCurrentLevel(), "Le niveau actuel doit correspondre.");
        assertEquals(requestedLevel, exception.getRequestedLevel(), "Le niveau demandé doit correspondre.");
        assertEquals("", exception.getReason(), "La raison du rejet devrait être une chaîne vide.");
    }

    // Test pour vérifier la création de l'exception avec toutes les informations
    // (message, niveaux, raison)
    @Test
    void invalidSubscriptionLevelChangeException_shouldBeCreatedWithAllInformation() {
        // GIVEN : Un message d'erreur, le niveau actuel, le niveau demandé et la raison
        // du rejet
        String errorMessage = "Le downgrade vers un niveau inférieur n'est pas autorisé pendant la période d'essai.";
        String currentLevel = "PREMIUM";
        String requestedLevel = "BASIC";
        String reason = "Période d'essai en cours.";

        // WHEN : Création d'une instance de InvalidSubscriptionLevelChangeException
        // avec le constructeur à quatre arguments (avec raison)
        InvalidSubscriptionLevelChangeException exception = new InvalidSubscriptionLevelChangeException(errorMessage,
                currentLevel, requestedLevel, reason);

        // THEN : Vérification que toutes les informations sont correctement définies
        assertEquals(errorMessage, exception.getMessage(), "Le message de l'exception doit correspondre.");
        assertNull(exception.getCause(), "La cause de l'exception devrait être nulle.");
        assertEquals(currentLevel, exception.getCurrentLevel(), "Le niveau actuel doit correspondre.");
        assertEquals(requestedLevel, exception.getRequestedLevel(), "Le niveau demandé doit correspondre.");
        assertEquals(reason, exception.getReason(), "La raison du rejet doit correspondre.");
    }

    // Test pour vérifier la création de l'exception avec toutes les informations
    // (message, cause, niveaux, raison)
    @Test
    void invalidSubscriptionLevelChangeException_shouldBeCreatedWithAllInformationIncludingCause() {
        // GIVEN : Un message d'erreur, une cause, le niveau actuel, le niveau demandé
        // et la raison du rejet
        String errorMessage = "Erreur lors de la vérification des règles de changement de niveau.";
        Throwable causeException = new NullPointerException("La configuration des règles est manquante.");
        String currentLevel = "BASIC";
        String requestedLevel = "PREMIUM";
        String reason = "Règle de changement de niveau non définie.";

        // WHEN : Création d'une instance de InvalidSubscriptionLevelChangeException
        // avec le constructeur à cinq arguments (avec cause et raison)
        InvalidSubscriptionLevelChangeException exception = new InvalidSubscriptionLevelChangeException(errorMessage,
                causeException, currentLevel, requestedLevel, reason);

        // THEN : Vérification que toutes les informations sont correctement définies
        assertEquals(errorMessage, exception.getMessage(), "Le message de l'exception doit correspondre.");
        assertEquals(causeException, exception.getCause(), "La cause de l'exception doit correspondre.");
        assertEquals(currentLevel, exception.getCurrentLevel(), "Le niveau actuel doit correspondre.");
        assertEquals(requestedLevel, exception.getRequestedLevel(), "Le niveau demandé doit correspondre.");
        assertEquals(reason, exception.getReason(), "La raison du rejet doit correspondre.");
    }

    // Test pour vérifier l'annotation @ResponseStatus sur la classe
    @Test
    void invalidSubscriptionLevelChangeException_shouldHaveBadRequestStatus() {
        // GIVEN : La classe InvalidSubscriptionLevelChangeException est annotée avec
        // @ResponseStatus

        // WHEN : Récupération de l'annotation ResponseStatus de la classe
        ResponseStatus responseStatus = InvalidSubscriptionLevelChangeException.class
                .getAnnotation(ResponseStatus.class);

        // THEN : Vérification que l'annotation est présente et que sa valeur est
        // HttpStatus.BAD_REQUEST
        assertEquals(HttpStatus.BAD_REQUEST, responseStatus.value(),
                "L'exception devrait avoir le statut HTTP BAD_REQUEST.");
    }

    // Test pour vérifier la récupération du niveau actuel via le getter
    @Test
    void getCurrentLevel_shouldReturnCorrectCurrentLevel() {
        // GIVEN : Création d'une exception avec un niveau actuel spécifique
        String currentLevel = "BASIC";
        InvalidSubscriptionLevelChangeException exception = new InvalidSubscriptionLevelChangeException(
                "Changement invalide.", currentLevel, "PREMIUM");

        // WHEN : Appel de la méthode getCurrentLevel()
        String retrievedLevel = exception.getCurrentLevel();

        // THEN : Vérification que la méthode retourne le niveau actuel correct
        assertEquals(currentLevel, retrievedLevel, "getCurrentLevel() devrait retourner le niveau actuel correct.");
    }

    // Test pour vérifier la récupération du niveau demandé via le getter
    @Test
    void getRequestedLevel_shouldReturnCorrectRequestedLevel() {
        // GIVEN : Création d'une exception avec un niveau demandé spécifique
        String requestedLevel = "ULTRA";
        InvalidSubscriptionLevelChangeException exception = new InvalidSubscriptionLevelChangeException(
                "Changement invalide.", "PREMIUM", requestedLevel);

        // WHEN : Appel de la méthode getRequestedLevel()
        String retrievedLevel = exception.getRequestedLevel();

        // THEN : Vérification que la méthode retourne le niveau demandé correct
        assertEquals(requestedLevel, retrievedLevel,
                "getRequestedLevel() devrait retourner le niveau demandé correct.");
    }

    // Test pour vérifier la récupération de la raison via le getter
    @Test
    void getReason_shouldReturnCorrectReason() {
        // GIVEN : Création d'une exception avec une raison spécifique
        String reason = "Non autorisé pour les comptes gratuits.";
        InvalidSubscriptionLevelChangeException exception = new InvalidSubscriptionLevelChangeException(
                "Changement invalide.", "BASIC", "PREMIUM", reason);

        // WHEN : Appel de la méthode getReason()
        String retrievedReason = exception.getReason();

        // THEN : Vérification que la méthode retourne la raison correcte
        assertEquals(reason, retrievedReason, "getReason() devrait retourner la raison correcte.");
    }
}