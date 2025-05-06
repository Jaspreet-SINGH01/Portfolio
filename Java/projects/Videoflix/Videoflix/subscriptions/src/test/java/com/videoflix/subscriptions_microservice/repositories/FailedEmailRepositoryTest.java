package com.videoflix.subscriptions_microservice.repositories;

import com.videoflix.subscriptions_microservice.entities.FailedEmail;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// @DataJpaTest est une annotation Spring Boot qui configure un environnement de test JPA en mémoire.
// Elle est utile pour tester les couches de persistance comme les repositories JPA.
@DataJpaTest
class FailedEmailRepositoryTest {

    // @Autowired permet d'injecter l'instance du repository que nous voulons
    // tester.
    @Autowired
    private FailedEmailRepository failedEmailRepository;

    // TestEntityManager est une utilitaire fourni par Spring Boot pour les tests
    // JPA.
    // Il offre des méthodes pour interagir avec la base de données de test
    // (persister, rechercher, etc.).
    @Autowired
    private TestEntityManager entityManager;

    // Test pour vérifier la méthode findByRecipientEmail du repository.
    @Test
    void findByRecipientEmail_shouldReturnFailedEmailsForGivenRecipient() {
        // GIVEN : Création et persistence de plusieurs entités FailedEmail avec
        // différents destinataires.
        FailedEmail failedEmail1 = new FailedEmail(null, null, null, 0, null, null);
        failedEmail1.setRecipientEmail("test@example.com");
        failedEmail1.setSubject("Test Email 1");
        failedEmail1.setBody("This is a test email.");
        failedEmail1.setCreationTimestamp(LocalDateTime.now());
        entityManager.persist(failedEmail1);

        FailedEmail failedEmail2 = new FailedEmail(null, null, null, 0, null, null);
        failedEmail2.setRecipientEmail("another@example.com");
        failedEmail2.setSubject("Another Email");
        failedEmail2.setBody("This is another email.");
        failedEmail2.setCreationTimestamp(LocalDateTime.now());
        entityManager.persist(failedEmail2);

        FailedEmail failedEmail3 = new FailedEmail(null, null, null, 0, null, null);
        failedEmail3.setRecipientEmail("test@example.com");
        failedEmail3.setSubject("Test Email 2");
        failedEmail3.setBody("This is another test email for the same recipient.");
        failedEmail3.setCreationTimestamp(LocalDateTime.now().minusHours(1));
        entityManager.persist(failedEmail3);

        entityManager.flush(); // Force la synchronisation de l'EntityManager avec la base de données.

        // WHEN : Appel de la méthode findByRecipientEmail avec l'email d'un des
        // destinataires.
        List<FailedEmail> foundEmails = failedEmailRepository.findByRecipientEmail("test@example.com");

        // THEN : Vérification que la liste retournée contient les entités FailedEmail
        // attendues pour ce destinataire.
        assertEquals(2, foundEmails.size());
        assertTrue(foundEmails.stream().anyMatch(email -> email.getSubject().equals("Test Email 1")));
        assertTrue(foundEmails.stream().anyMatch(email -> email.getSubject().equals("Test Email 2")));
    }

    // Test pour vérifier la méthode findByRecipientEmail lorsque aucun email n'a
    // été trouvé pour le destinataire.
    @Test
    void findByRecipientEmail_shouldReturnEmptyListIfNoMatch() {
        // GIVEN : Création et persistence d'une entité FailedEmail avec un destinataire
        // spécifique.
        FailedEmail failedEmail = new FailedEmail(null, null, null, 0, null, null);
        failedEmail.setRecipientEmail("unique@example.com");
        failedEmail.setSubject("Unique Email");
        failedEmail.setBody("This is a unique email.");
        failedEmail.setCreationTimestamp(LocalDateTime.now());
        entityManager.persist(failedEmail);
        entityManager.flush();

        // WHEN : Appel de la méthode findByRecipientEmail avec un email qui n'existe
        // pas dans la base de données.
        List<FailedEmail> foundEmails = failedEmailRepository.findByRecipientEmail("nonexistent@example.com");

        // THEN : Vérification que la liste retournée est vide.
        assertTrue(foundEmails.isEmpty());
    }

    // Test pour vérifier la méthode findByCreationTimestampBefore du repository.
    @Test
    void findByCreationTimestampBefore_shouldReturnFailedEmailsCreatedBeforeGivenDateTime() {
        // GIVEN : Création et persistence de plusieurs entités FailedEmail avec
        // différentes dates de création.
        LocalDateTime now = LocalDateTime.now();

        FailedEmail failedEmail1 = new FailedEmail(null, null, null, 0, null, now);
        failedEmail1.setRecipientEmail("old1@example.com");
        failedEmail1.setSubject("Old Email 1");
        failedEmail1.setBody("This is an old email.");
        failedEmail1.setCreationTimestamp(now.minusDays(2));
        entityManager.persist(failedEmail1);

        FailedEmail failedEmail2 = new FailedEmail(null, null, null, 0, null, now);
        failedEmail2.setRecipientEmail("recent@example.com");
        failedEmail2.setSubject("Recent Email");
        failedEmail2.setBody("This is a recent email.");
        failedEmail2.setCreationTimestamp(now.minusHours(1));
        entityManager.persist(failedEmail2);

        FailedEmail failedEmail3 = new FailedEmail(null, null, null, 0, null, now);
        failedEmail3.setRecipientEmail("old2@example.com");
        failedEmail3.setSubject("Old Email 2");
        failedEmail3.setBody("This is another old email.");
        failedEmail3.setCreationTimestamp(now.minusDays(1));
        entityManager.persist(failedEmail3);

        FailedEmail failedEmail4 = new FailedEmail(null, null, null, 0, null, now);
        failedEmail4.setRecipientEmail("future@example.com");
        failedEmail4.setSubject("Future Email");
        failedEmail4.setBody("This is a future email.");
        failedEmail4.setCreationTimestamp(now.plusHours(1));
        entityManager.persist(failedEmail4);

        entityManager.flush();

        // WHEN : Appel de la méthode findByCreationTimestampBefore avec une date dans
        // le passé (maintenant).
        List<FailedEmail> foundEmails = failedEmailRepository.findByCreationTimestampBefore(now);

        // THEN : Vérification que la liste retournée contient les entités créées avant
        // cette date.
        assertEquals(3, foundEmails.size());
        assertTrue(foundEmails.stream().anyMatch(email -> email.getSubject().equals("Old Email 1")));
        assertTrue(foundEmails.stream().anyMatch(email -> email.getSubject().equals("Recent Email")));
        assertTrue(foundEmails.stream().anyMatch(email -> email.getSubject().equals("Old Email 2")));
    }

    // Test pour vérifier la méthode findByCreationTimestampBefore lorsque aucune
    // entité n'est antérieure à la date donnée.
    @Test
    void findByCreationTimestampBefore_shouldReturnEmptyListIfNoEmailsBeforeGivenTime() {
        // GIVEN : Création et persistence d'une entité FailedEmail avec une date de
        // création dans le passé.
        LocalDateTime past = LocalDateTime.now().minusDays(3);
        FailedEmail failedEmail = new FailedEmail(null, null, null, 0, null, past);
        failedEmail.setRecipientEmail("past@example.com");
        failedEmail.setSubject("Past Email");
        failedEmail.setBody("This is a past email.");
        failedEmail.setCreationTimestamp(past);
        entityManager.persist(failedEmail);
        entityManager.flush();

        // WHEN : Appel de la méthode findByCreationTimestampBefore avec une date encore
        // plus ancienne.
        LocalDateTime older = past.minusDays(2);
        List<FailedEmail> foundEmails = failedEmailRepository.findByCreationTimestampBefore(older);

        // THEN : Vérification que la liste retournée est vide car l'email persistant
        // n'est pas antérieur à 'older'.
        assertTrue(foundEmails.isEmpty());
    }
}