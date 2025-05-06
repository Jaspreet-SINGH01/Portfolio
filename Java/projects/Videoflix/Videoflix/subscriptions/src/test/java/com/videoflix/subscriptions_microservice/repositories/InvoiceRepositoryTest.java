package com.videoflix.subscriptions_microservice.repositories;

import com.videoflix.subscriptions_microservice.entities.Invoice;
import com.videoflix.subscriptions_microservice.entities.Invoice.InvoiceStatus;
import com.videoflix.subscriptions_microservice.entities.Subscription;
import com.videoflix.subscriptions_microservice.entities.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

// @DataJpaTest est une annotation Spring Boot pour tester les composants JPA.
// Elle configure une base de données en mémoire, un EntityManager et un repository JPA.
@DataJpaTest
class InvoiceRepositoryTest {

    // @Autowired injecte l'instance du repository que nous voulons tester.
    @Autowired
    private InvoiceRepository invoiceRepository;

    // TestEntityManager est un utilitaire pour interagir avec la base de données de
    // test.
    @Autowired
    private TestEntityManager entityManager;

    // Méthode utilitaire pour créer et persister une entité Invoice pour les tests.
    private Invoice createAndPersistInvoice(Long subscriptionId, Long userId, String stripeInvoiceId,
            InvoiceStatus status, LocalDateTime issueDate) {
        Invoice invoice = new Invoice();
        Subscription subscription = new Subscription();
        subscription.setId(subscriptionId);
        invoice.setSubscription(subscription);
        User user = new User();
        user.setId(userId);
        invoice.setUser(user);
        invoice.setStripeInvoiceId(stripeInvoiceId);
        invoice.setStatus(status);
        invoice.setIssueDate(issueDate);
        return entityManager.persistAndFlush(invoice);
    }

    // Test pour vérifier la méthode findBySubscriptionId.
    @Test
    void findBySubscriptionId_shouldReturnInvoicesForGivenSubscriptionId() {
        // GIVEN : Création et persistence de plusieurs factures avec différents IDs
        // d'abonnement.
        createAndPersistInvoice(1L, 10L, "stripe_1", InvoiceStatus.PAID, LocalDateTime.now().minusDays(2));
        // WHEN : Recherche des factures par un ID d'abonnement spécifique (1L).
        List<Invoice> invoices = invoiceRepository.findBySubscriptionId(1L);

        // THEN : Vérification que la liste contient les deux factures associées à l'ID
        // d'abonnement 1L.
        assertEquals(2, invoices.size());
        assertTrue(invoices.stream().anyMatch(invoice -> invoice.getStripeInvoiceId().equals("stripe_1")));
        assertTrue(invoices.stream().anyMatch(invoice -> invoice.getStripeInvoiceId().equals("stripe_3")));
        assertFalse(invoices.stream().anyMatch(invoice -> invoice.getStripeInvoiceId().equals("stripe_2")));
    }

    // Test pour vérifier la méthode findByUserId.
    @Test
    void findByUserId_shouldReturnInvoicesForGivenUserId() {
        // GIVEN : Création et persistence de plusieurs factures avec différents IDs
        // d'utilisateur.
        createAndPersistInvoice(1L, 10L, "stripe_1", InvoiceStatus.PAID, LocalDateTime.now().minusDays(2));

        // WHEN : Recherche des factures par un ID d'utilisateur spécifique (10L).
        List<Invoice> invoices = invoiceRepository.findByUserId(10L);

        // THEN : Vérification que la liste contient les deux factures associées à l'ID
        // d'utilisateur 10L.
        assertEquals(2, invoices.size());
        assertTrue(invoices.stream().anyMatch(invoice -> invoice.getStripeInvoiceId().equals("stripe_1")));
        assertTrue(invoices.stream().anyMatch(invoice -> invoice.getStripeInvoiceId().equals("stripe_3")));
        assertFalse(invoices.stream().anyMatch(invoice -> invoice.getStripeInvoiceId().equals("stripe_2")));
    }

    // Test pour vérifier la méthode findByStripeInvoiceId.
    @Test
    void findByStripeInvoiceId_shouldReturnInvoiceForGivenStripeInvoiceId() {
        // GIVEN : Création et persistence de plusieurs factures avec différents IDs
        // Stripe.
        createAndPersistInvoice(1L, 10L, "stripe_1", InvoiceStatus.PAID, LocalDateTime.now().minusDays(2));
        Invoice expectedInvoice = createAndPersistInvoice(2L, 11L, "unique_stripe_id", InvoiceStatus.PENDING,
                LocalDateTime.now().minusDays(1));
        createAndPersistInvoice(1L, 10L, "stripe_3", InvoiceStatus.PAID, LocalDateTime.now());

        // WHEN : Recherche d'une facture par son ID Stripe unique.
        Optional<Invoice> foundInvoice = invoiceRepository.findByStripeInvoiceId("unique_stripe_id");

        // THEN : Vérification qu'une Optional contenant la facture attendue est
        // retournée.
        assertTrue(foundInvoice.isPresent());
        assertEquals(expectedInvoice.getId(), foundInvoice.get().getId());
    }

    // Test pour vérifier la méthode findByStripeInvoiceId lorsqu'aucun
    // enregistrement ne correspond.
    @Test
    void findByStripeInvoiceId_shouldReturnEmptyOptionalIfNoMatch() {
        // GIVEN : Aucune facture avec l'ID Stripe recherché n'est persistée.

        // WHEN : Recherche d'une facture par un ID Stripe inexistant.
        Optional<Invoice> foundInvoice = invoiceRepository.findByStripeInvoiceId("non_existent_stripe_id");

        // THEN : Vérification qu'une Optional vide est retournée.
        assertTrue(foundInvoice.isEmpty());
    }

    // Test pour vérifier la méthode findByStatus.
    @Test
    void findByStatus_shouldReturnInvoicesForGivenStatus() {
        // GIVEN : Création et persistence de plusieurs factures avec différents
        // statuts.
        createAndPersistInvoice(1L, 10L, "stripe_1", InvoiceStatus.PAID, LocalDateTime.now().minusDays(2));
        createAndPersistInvoice(3L, 12L, "stripe_4", InvoiceStatus.FAILED, LocalDateTime.now().minusHours(1));

        // WHEN : Recherche des factures avec le statut PAID.
        List<Invoice> paidInvoices = invoiceRepository.findByStatus(InvoiceStatus.PAID);

        // THEN : Vérification que la liste contient les deux factures avec le statut
        // PAID.
        assertEquals(2, paidInvoices.size());
        assertTrue(paidInvoices.stream().anyMatch(invoice -> invoice.getStripeInvoiceId().equals("stripe_1")));
        assertTrue(paidInvoices.stream().anyMatch(invoice -> invoice.getStripeInvoiceId().equals("stripe_3")));
        assertFalse(paidInvoices.stream().anyMatch(invoice -> invoice.getStripeInvoiceId().equals("stripe_2")));
        assertFalse(paidInvoices.stream().anyMatch(invoice -> invoice.getStripeInvoiceId().equals("stripe_4")));
    }

    // Test pour vérifier la méthode findByIssueDateBetween.
    @Test
    void findByIssueDateBetween_shouldReturnInvoicesWithinGivenDateRange() {
        // GIVEN : Création et persistence de plusieurs factures avec différentes dates
        // d'émission.
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate = now.minusDays(3);
        LocalDateTime endDate = now.minusDays(1);

        createAndPersistInvoice(1L, 10L, "stripe_1", InvoiceStatus.PAID, now.minusDays(4)); // En dehors de la plage
        createAndPersistInvoice(3L, 12L, "stripe_4", InvoiceStatus.FAILED, now); // En dehors de la plage

        // WHEN : Recherche des factures émises entre startDate et endDate.
        List<Invoice> invoicesInRange = invoiceRepository.findByIssueDateBetween(startDate, endDate);

        // THEN : Vérification que la liste contient les deux factures dont la date
        // d'émission est dans la plage spécifiée.
        assertEquals(2, invoicesInRange.size());
        assertTrue(invoicesInRange.stream().anyMatch(invoice -> invoice.getStripeInvoiceId().equals("stripe_2")));
        assertTrue(invoicesInRange.stream().anyMatch(invoice -> invoice.getStripeInvoiceId().equals("stripe_3")));
        assertFalse(invoicesInRange.stream().anyMatch(invoice -> invoice.getStripeInvoiceId().equals("stripe_1")));
        assertFalse(invoicesInRange.stream().anyMatch(invoice -> invoice.getStripeInvoiceId().equals("stripe_4")));
    }

    // Test pour vérifier findByIssueDateBetween lorsque aucune facture n'est dans
    // la plage.
    @Test
    void findByIssueDateBetween_shouldReturnEmptyListIfNoInvoicesInDateRange() {
        // GIVEN : Création d'une facture en dehors de la plage de dates spécifiée.
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate = now.minusDays(3);
        LocalDateTime endDate = now.minusDays(1);
        createAndPersistInvoice(1L, 10L, "stripe_1", InvoiceStatus.PAID, now.minusDays(4));

        // WHEN : Recherche des factures dans la plage.
        List<Invoice> invoicesInRange = invoiceRepository.findByIssueDateBetween(startDate, endDate);

        // THEN : Vérification que la liste retournée est vide.
        assertTrue(invoicesInRange.isEmpty());
    }
}