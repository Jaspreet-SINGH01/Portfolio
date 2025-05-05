package com.videoflix.subscriptions_microservice.entities;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class FailedEmailTest {

    // Test pour vérifier la création d'un objet FailedEmail avec le constructeur et
    // la récupération des valeurs
    @Test
    void failedEmail_constructorSetsValuesCorrectly() {
        // GIVEN : Création des valeurs pour instancier un FailedEmail
        String recipientEmail = "test@example.com";
        String subject = "Email Delivery Failed";
        String body = "This is the content of the failed email.";
        int attemptCount = 3;
        String failureReason = "SMTP server unavailable";
        LocalDateTime creationTimestamp = LocalDateTime.now();

        // WHEN : Création d'un objet FailedEmail en utilisant le constructeur
        FailedEmail failedEmail = new FailedEmail(recipientEmail, subject, body, attemptCount, failureReason,
                creationTimestamp);

        // THEN : Vérification que les valeurs passées au constructeur sont correctement
        // attribuées aux champs de l'objet
        assertEquals(recipientEmail, failedEmail.getRecipientEmail(), "L'e-mail du destinataire doit correspondre.");
        assertEquals(subject, failedEmail.getSubject(), "Le sujet doit correspondre.");
        assertEquals(body, failedEmail.getBody(), "Le corps de l'e-mail doit correspondre.");
        assertEquals(attemptCount, failedEmail.getAttemptCount(), "Le nombre de tentatives doit correspondre.");
        assertEquals(failureReason, failedEmail.getFailureReason(), "La raison de l'échec doit correspondre.");
        assertEquals(creationTimestamp, failedEmail.getCreationTimestamp(),
                "L'horodatage de création doit correspondre.");
    }

    // Test pour vérifier le fonctionnement de la méthode toString()
    @Test
    void failedEmail_toStringMethodReturnsExpectedFormat() {
        // GIVEN : Création d'un objet FailedEmail
        String recipientEmail = "another@example.com";
        String subject = "Second Failure";
        String body = "Another email failed to send.";
        int attemptCount = 1;
        String failureReason = "Connection timeout";
        LocalDateTime creationTimestamp = LocalDateTime.of(2023, 1, 1, 10, 0);
        FailedEmail failedEmail = new FailedEmail(recipientEmail, subject, body, attemptCount, failureReason,
                creationTimestamp);

        // WHEN : Appel de la méthode toString()
        String toStringResult = failedEmail.toString();

        // THEN : Vérification que la chaîne résultante contient les informations
        // attendues dans un format lisible
        assertNotNull(toStringResult, "Le résultat de toString ne doit pas être null.");
        assertEquals(
                "FailedEmail{id=null, recipientEmail='another@example.com', subject='Second Failure', body='Another email failed to send.', attemptCount=1, failureReason='Connection timeout', creationTimestamp=2023-01-01T10:00}",
                toStringResult, "La représentation en chaîne ne correspond pas au format attendu.");
        // Note : L'ID sera null car il est généré par la base de données et n'est pas
        // défini lors de la création de l'objet pour le test.
    }

    // Test pour vérifier que les getters fonctionnent correctement (bien que
    // lombok.Data les génère)
    @Test
    void failedEmail_gettersReturnCorrectValues() {
        // GIVEN : Création d'un objet FailedEmail
        String recipientEmail = "getter.test@example.com";
        String subject = "Getter Test";
        String body = "Testing the getter methods.";
        int attemptCount = 2;
        String failureReason = "Authentication failed";
        LocalDateTime creationTimestamp = LocalDateTime.now().minusHours(1);
        FailedEmail failedEmail = new FailedEmail(recipientEmail, subject, body, attemptCount, failureReason,
                creationTimestamp);

        // THEN : Vérification que chaque getter retourne la valeur correcte
        assertEquals(recipientEmail, failedEmail.getRecipientEmail(),
                "getRecipientEmail() doit retourner la valeur correcte.");
        assertEquals(subject, failedEmail.getSubject(), "getSubject() doit retourner la valeur correcte.");
        assertEquals(body, failedEmail.getBody(), "getBody() doit retourner la valeur correcte.");
        assertEquals(attemptCount, failedEmail.getAttemptCount(),
                "getAttemptCount() doit retourner la valeur correcte.");
        assertEquals(failureReason, failedEmail.getFailureReason(),
                "getFailureReason() doit retourner la valeur correcte.");
        assertEquals(creationTimestamp, failedEmail.getCreationTimestamp(),
                "getCreationTimestamp() doit retourner la valeur correcte.");
    }
}