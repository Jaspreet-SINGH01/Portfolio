package com.videoflix.subscriptions_microservice.exceptions;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResourceConflictExceptionTest {

    // Test pour vérifier la création de l'exception avec un message uniquement
    @Test
    void resourceConflictException_shouldBeCreatedWithMessageOnly() {
        // GIVEN : Un message indiquant un conflit de ressource
        String message = "La ressource existe déjà.";

        // WHEN : Création d'une instance de ResourceConflictException avec le
        // constructeur à un seul argument
        ResourceConflictException exception = new ResourceConflictException(message);

        // THEN : Vérification que le message est correctement défini et que les autres
        // attributs sont initialisés par défaut
        assertEquals(message, exception.getMessage(), "Le message de l'exception doit correspondre.");
        assertNull(exception.getCause(), "La cause de l'exception devrait être nulle.");
        assertEquals("", exception.getResourceId(), "L'ID de la ressource devrait être une chaîne vide.");
        assertEquals("", exception.getConflictingField(), "Le champ en conflit devrait être une chaîne vide.");
        assertTrue(exception.getConflictingValue() instanceof Object, "La valeur en conflit devrait être un objet.");
    }

    // Test pour vérifier la création de l'exception avec un message et une cause
    @Test
    void resourceConflictException_shouldBeCreatedWithMessageAndCause() {
        // GIVEN : Un message et une exception cause
        String message = "Erreur lors de la tentative de création de la ressource.";
        Throwable causeException = new RuntimeException("Violation de contrainte d'unicité.");

        // WHEN : Création d'une instance de ResourceConflictException avec le
        // constructeur à deux arguments
        ResourceConflictException exception = new ResourceConflictException(message, causeException);

        // THEN : Vérification que le message et la cause sont correctement définis et
        // que les autres attributs sont initialisés par défaut
        assertEquals(message, exception.getMessage(), "Le message de l'exception doit correspondre.");
        assertEquals(causeException, exception.getCause(), "La cause de l'exception doit correspondre.");
        assertEquals("", exception.getResourceId(), "L'ID de la ressource devrait être une chaîne vide.");
        assertEquals("", exception.getConflictingField(), "Le champ en conflit devrait être une chaîne vide.");
        assertTrue(exception.getConflictingValue() instanceof Object, "La valeur en conflit devrait être un objet.");
    }

    // Test pour vérifier la création de l'exception avec toutes les informations
    // (message, ID, champ, valeur)
    @Test
    void resourceConflictException_shouldBeCreatedWithAllInformation() {
        // GIVEN : Un message, l'ID de la ressource, le champ en conflit et la valeur en
        // conflit
        String message = "Un abonnement avec cet e-mail existe déjà.";
        String resourceId = "user_123";
        String conflictingField = "email";
        String conflictingValue = "existing@example.com";

        // WHEN : Création d'une instance de ResourceConflictException avec le
        // constructeur à quatre arguments
        ResourceConflictException exception = new ResourceConflictException(message, resourceId, conflictingField,
                conflictingValue);

        // THEN : Vérification que tous les attributs sont correctement définis
        assertEquals(message, exception.getMessage(), "Le message de l'exception doit correspondre.");
        assertNull(exception.getCause(), "La cause de l'exception devrait être nulle.");
        assertEquals(resourceId, exception.getResourceId(), "L'ID de la ressource doit correspondre.");
        assertEquals(conflictingField, exception.getConflictingField(), "Le champ en conflit doit correspondre.");
        assertEquals(conflictingValue, exception.getConflictingValue(), "La valeur en conflit doit correspondre.");
    }

    // Test pour vérifier la création de l'exception avec toutes les informations
    // incluant la cause
    @Test
    void resourceConflictException_shouldBeCreatedWithCauseAndAllInformation() {
        // GIVEN : Un message, une cause, l'ID de la ressource, le champ en conflit et
        // la valeur en conflit
        String message = "Erreur lors de la vérification de l'unicité.";
        Throwable causeException = new RuntimeException("Erreur de base de données.");
        String resourceId = "subscription_456";
        String conflictingField = "customer_id";
        String conflictingValue = "cust_789";

        // WHEN : Création d'une instance de ResourceConflictException avec le
        // constructeur à cinq arguments
        ResourceConflictException exception = new ResourceConflictException(message, causeException, resourceId,
                conflictingField, conflictingValue);

        // THEN : Vérification que tous les attributs sont correctement définis
        assertEquals(message, exception.getMessage(), "Le message de l'exception doit correspondre.");
        assertEquals(causeException, exception.getCause(), "La cause de l'exception doit correspondre.");
        assertEquals(resourceId, exception.getResourceId(), "L'ID de la ressource doit correspondre.");
        assertEquals(conflictingField, exception.getConflictingField(), "Le champ en conflit doit correspondre.");
        assertEquals(conflictingValue, exception.getConflictingValue(), "La valeur en conflit doit correspondre.");
    }

    // Test pour vérifier l'annotation @ResponseStatus sur la classe
    @Test
    void resourceConflictException_shouldHaveConflictStatus() {
        // GIVEN : La classe ResourceConflictException est annotée avec @ResponseStatus

        // WHEN : Récupération de l'annotation ResponseStatus de la classe
        ResponseStatus responseStatus = ResourceConflictException.class.getAnnotation(ResponseStatus.class);

        // THEN : Vérification que l'annotation est présente et que sa valeur est
        // HttpStatus.CONFLICT
        assertEquals(HttpStatus.CONFLICT, responseStatus.value(), "L'exception devrait avoir le statut HTTP CONFLICT.");
    }

    // Test pour vérifier la récupération de l'ID de la ressource via le getter
    @Test
    void getResourceId_shouldReturnCorrectResourceId() {
        // GIVEN : Création d'une exception avec un ID de ressource spécifique
        String resourceId = "promo_abc";
        ResourceConflictException exception = new ResourceConflictException("Conflit de promotion.", resourceId, "code",
                "SUMMER20");

        // WHEN : Appel de la méthode getResourceId()
        String retrievedId = exception.getResourceId();

        // THEN : Vérification que la méthode retourne l'ID de la ressource correct
        assertEquals(resourceId, retrievedId, "getResourceId() devrait retourner l'ID de la ressource correct.");
    }

    // Test pour vérifier la récupération du champ en conflit via le getter
    @Test
    void getConflictingField_shouldReturnCorrectConflictingField() {
        // GIVEN : Création d'une exception avec un champ en conflit spécifique
        String conflictingField = "username";
        ResourceConflictException exception = new ResourceConflictException("Nom d'utilisateur déjà pris.", "user_xyz",
                conflictingField, "existingUser");

        // WHEN : Appel de la méthode getConflictingField()
        String retrievedField = exception.getConflictingField();

        // THEN : Vérification que la méthode retourne le champ en conflit correct
        assertEquals(conflictingField, retrievedField,
                "getConflictingField() devrait retourner le champ en conflit correct.");
    }

    // Test pour vérifier la récupération de la valeur en conflit via le getter
    @Test
    void getConflictingValue_shouldReturnCorrectConflictingValue() {
        // GIVEN : Création d'une exception avec une valeur en conflit spécifique
        Object conflictingValue = 123L;
        ResourceConflictException exception = new ResourceConflictException("L'ID existe déjà.", "payment_99",
                "paymentId", conflictingValue);

        // WHEN : Appel de la méthode getConflictingValue()
        Object retrievedValue = exception.getConflictingValue();

        // THEN : Vérification que la méthode retourne la valeur en conflit correcte
        assertEquals(conflictingValue, retrievedValue,
                "getConflictingValue() devrait retourner la valeur en conflit correcte.");
    }
}