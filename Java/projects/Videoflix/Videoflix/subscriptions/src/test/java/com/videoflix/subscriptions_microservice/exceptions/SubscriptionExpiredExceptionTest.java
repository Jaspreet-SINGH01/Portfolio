package com.videoflix.subscriptions_microservice.exceptions;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class SubscriptionExpiredExceptionTest {

    // Test pour vérifier la création de l'exception avec un message uniquement
    @Test
    void subscriptionExpiredException_shouldBeCreatedWithMessageOnly() {
        // GIVEN : Un message indiquant que l'abonnement est expiré
        String message = "L'abonnement a expiré.";

        // WHEN : Création d'une instance de SubscriptionExpiredException avec le
        // constructeur à un seul argument
        SubscriptionExpiredException exception = new SubscriptionExpiredException(message);

        // THEN : Vérification que le message est correctement défini et que la date
        // d'expiration est nulle
        assertEquals(message, exception.getMessage(), "Le message de l'exception doit correspondre.");
        assertNull(exception.getCause(), "La cause de l'exception devrait être nulle.");
        assertNull(exception.getExpirationDate(), "La date d'expiration devrait être nulle.");
    }

    // Test pour vérifier la création de l'exception avec un message et une cause
    @Test
    void subscriptionExpiredException_shouldBeCreatedWithMessageAndCause() {
        // GIVEN : Un message et une exception cause
        String message = "Impossible d'effectuer l'opération car l'abonnement est expiré.";
        Throwable causeException = new IllegalStateException("L'abonnement n'est plus actif.");

        // WHEN : Création d'une instance de SubscriptionExpiredException avec le
        // constructeur à deux arguments
        SubscriptionExpiredException exception = new SubscriptionExpiredException(message, causeException);

        // THEN : Vérification que le message et la cause sont correctement définis et
        // que la date d'expiration est nulle
        assertEquals(message, exception.getMessage(), "Le message de l'exception doit correspondre.");
        assertEquals(causeException, exception.getCause(), "La cause de l'exception doit correspondre.");
        assertNull(exception.getExpirationDate(), "La date d'expiration devrait être nulle.");
    }

    // Test pour vérifier la création de l'exception avec un message et une date
    // d'expiration
    @Test
    void subscriptionExpiredException_shouldBeCreatedWithMessageAndExpirationDate() {
        // GIVEN : Un message et une date d'expiration spécifique
        String message = "L'abonnement a expiré le 2025-05-05.";
        LocalDate expirationDate = LocalDate.of(2025, 5, 5);

        // WHEN : Création d'une instance de SubscriptionExpiredException avec le
        // constructeur à deux arguments (message et expirationDate)
        SubscriptionExpiredException exception = new SubscriptionExpiredException(message, expirationDate);

        // THEN : Vérification que le message et la date d'expiration sont correctement
        // définis
        assertEquals(message, exception.getMessage(), "Le message de l'exception doit correspondre.");
        assertNull(exception.getCause(), "La cause de l'exception devrait être nulle.");
        assertEquals(expirationDate, exception.getExpirationDate(), "La date d'expiration doit correspondre.");
    }

    // Test pour vérifier la création de l'exception avec un message, une cause et
    // une date d'expiration
    @Test
    void subscriptionExpiredException_shouldBeCreatedWithMessageCauseAndExpirationDate() {
        // GIVEN : Un message, une cause et une date d'expiration spécifique
        String message = "Tentative d'accès à une fonctionnalité après l'expiration de l'abonnement.";
        Throwable causeException = new SecurityException("L'accès n'est plus autorisé.");
        LocalDate expirationDate = LocalDate.of(2025, 5, 1);

        // WHEN : Création d'une instance de SubscriptionExpiredException avec le
        // constructeur à trois arguments (message, cause et expirationDate)
        SubscriptionExpiredException exception = new SubscriptionExpiredException(message, causeException,
                expirationDate);

        // THEN : Vérification que le message, la cause et la date d'expiration sont
        // correctement définis
        assertEquals(message, exception.getMessage(), "Le message de l'exception doit correspondre.");
        assertEquals(causeException, exception.getCause(), "La cause de l'exception doit correspondre.");
        assertEquals(expirationDate, exception.getExpirationDate(), "La date d'expiration doit correspondre.");
    }

    // Test pour vérifier l'annotation @ResponseStatus sur la classe
    @Test
    void subscriptionExpiredException_shouldHaveForbiddenStatus() {
        // GIVEN : La classe SubscriptionExpiredException est annotée avec
        // @ResponseStatus

        // WHEN : Récupération de l'annotation ResponseStatus de la classe
        ResponseStatus responseStatus = SubscriptionExpiredException.class.getAnnotation(ResponseStatus.class);

        // THEN : Vérification que l'annotation est présente et que sa valeur est
        // HttpStatus.FORBIDDEN
        assertEquals(HttpStatus.FORBIDDEN, responseStatus.value(),
                "L'exception devrait avoir le statut HTTP FORBIDDEN.");
    }

    // Test pour vérifier la récupération de la date d'expiration via le getter
    @Test
    void getExpirationDate_shouldReturnCorrectExpirationDate() {
        // GIVEN : Création d'une exception avec une date d'expiration spécifique
        LocalDate expirationDate = LocalDate.now().plusDays(30);
        SubscriptionExpiredException exception = new SubscriptionExpiredException("L'abonnement expirera bientôt.",
                expirationDate);

        // WHEN : Appel de la méthode getExpirationDate()
        LocalDate retrievedDate = exception.getExpirationDate();

        // THEN : Vérification que la méthode retourne la date d'expiration correcte
        assertEquals(expirationDate, retrievedDate,
                "getExpirationDate() devrait retourner la date d'expiration correcte.");
    }
}