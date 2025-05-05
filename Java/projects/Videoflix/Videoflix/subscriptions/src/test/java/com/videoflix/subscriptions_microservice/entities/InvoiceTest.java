package com.videoflix.subscriptions_microservice.entities;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class InvoiceTest {

    // Test pour vérifier la création d'une facture avec le constructeur
    // AllArgsConstructor
    @Test
    void invoice_allArgsConstructor_shouldSetValuesCorrectly() {
        // GIVEN : Création d'objets mockés pour les relations ManyToOne
        Subscription mockSubscription = Mockito.mock(Subscription.class);
        User mockUser = Mockito.mock(User.class);
        LocalDateTime issueDate = LocalDateTime.now();
        LocalDateTime billingStartDate = LocalDateTime.now().minusDays(30);
        LocalDateTime billingEndDate = LocalDateTime.now();
        Double amount = 9.99;
        String currency = "EUR";
        Invoice.InvoiceStatus status = Invoice.InvoiceStatus.PAID;
        String stripeInvoiceId = "stripe_123";
        LocalDateTime paymentDate = LocalDateTime.now().minusDays(5);
        String paymentMethod = "Credit Card";
        String transactionId = "txn_456";
        String notes = "Payment successful.";

        // WHEN : Création d'une instance d'Invoice en utilisant le constructeur
        // AllArgsConstructor
        Invoice invoice = new Invoice(null, mockSubscription, mockUser, issueDate, billingStartDate, billingEndDate,
                amount, currency, status, stripeInvoiceId, paymentDate, paymentMethod, transactionId, notes);

        // THEN : Vérification que tous les champs ont été correctement initialisés
        assertNull(invoice.getId(), "L'ID devrait être null avant la persistance.");
        assertEquals(mockSubscription, invoice.getSubscription(), "L'abonnement doit correspondre.");
        assertEquals(mockUser, invoice.getUser(), "L'utilisateur doit correspondre.");
        assertEquals(issueDate, invoice.getIssueDate(), "La date d'émission doit correspondre.");
        assertEquals(billingStartDate, invoice.getBillingStartDate(),
                "La date de début de la facturation doit correspondre.");
        assertEquals(billingEndDate, invoice.getBillingEndDate(),
                "La date de fin de la facturation doit correspondre.");
        assertEquals(amount, invoice.getAmount(), "Le montant doit correspondre.");
        assertEquals(currency, invoice.getCurrency(), "La devise doit correspondre.");
        assertEquals(status, invoice.getStatus(), "Le statut doit correspondre.");
        assertEquals(stripeInvoiceId, invoice.getStripeInvoiceId(), "L'ID de facture Stripe doit correspondre.");
        assertEquals(paymentDate, invoice.getPaymentDate(), "La date de paiement doit correspondre.");
        assertEquals(paymentMethod, invoice.getPaymentMethod(), "La méthode de paiement doit correspondre.");
        assertEquals(transactionId, invoice.getTransactionId(), "L'ID de transaction doit correspondre.");
        assertEquals(notes, invoice.getNotes(), "Les notes doivent correspondre.");
    }

    // Test pour vérifier la création d'une facture avec le constructeur
    // NoArgsConstructor et les setters
    @Test
    void invoice_noArgsConstructorAndSetters_shouldSetValuesCorrectly() {
        // GIVEN : Création d'une instance d'Invoice en utilisant le constructeur
        // NoArgsConstructor
        Invoice invoice = new Invoice();
        Subscription mockSubscription = Mockito.mock(Subscription.class);
        User mockUser = Mockito.mock(User.class);
        LocalDateTime issueDate = LocalDateTime.now();
        LocalDateTime billingStartDate = LocalDateTime.now().minusDays(30);
        LocalDateTime billingEndDate = LocalDateTime.now();
        Double amount = 9.99;
        String currency = "EUR";
        Invoice.InvoiceStatus status = Invoice.InvoiceStatus.PAID;
        String stripeInvoiceId = "stripe_123";
        LocalDateTime paymentDate = LocalDateTime.now().minusDays(5);
        String paymentMethod = "Credit Card";
        String transactionId = "txn_456";
        String notes = "Payment successful.";

        // WHEN : Utilisation des setters pour attribuer des valeurs aux champs
        invoice.setSubscription(mockSubscription);
        invoice.setUser(mockUser);
        invoice.setIssueDate(issueDate);
        invoice.setBillingStartDate(billingStartDate);
        invoice.setBillingEndDate(billingEndDate);
        invoice.setAmount(amount);
        invoice.setCurrency(currency);
        invoice.setStatus(status);
        invoice.setStripeInvoiceId(stripeInvoiceId);
        invoice.setPaymentDate(paymentDate);
        invoice.setPaymentMethod(paymentMethod);
        invoice.setTransactionId(transactionId);
        invoice.setNotes(notes);

        // THEN : Vérification que tous les champs ont été correctement définis via les
        // setters
        assertNull(invoice.getId(), "L'ID devrait être null avant la persistance.");
        assertEquals(mockSubscription, invoice.getSubscription(), "L'abonnement doit correspondre.");
        assertEquals(mockUser, invoice.getUser(), "L'utilisateur doit correspondre.");
        assertEquals(issueDate, invoice.getIssueDate(), "La date d'émission doit correspondre.");
        assertEquals(billingStartDate, invoice.getBillingStartDate(),
                "La date de début de la facturation doit correspondre.");
        assertEquals(billingEndDate, invoice.getBillingEndDate(),
                "La date de fin de la facturation doit correspondre.");
        assertEquals(amount, invoice.getAmount(), "Le montant doit correspondre.");
        assertEquals(currency, invoice.getCurrency(), "La devise doit correspondre.");
        assertEquals(status, invoice.getStatus(), "Le statut doit correspondre.");
        assertEquals(stripeInvoiceId, invoice.getStripeInvoiceId(), "L'ID de facture Stripe doit correspondre.");
        assertEquals(paymentDate, invoice.getPaymentDate(), "La date de paiement doit correspondre.");
        assertEquals(paymentMethod, invoice.getPaymentMethod(), "La méthode de paiement doit correspondre.");
        assertEquals(transactionId, invoice.getTransactionId(), "L'ID de transaction doit correspondre.");
        assertEquals(notes, invoice.getNotes(), "Les notes doivent correspondre.");
    }

    // Test pour vérifier l'énumération InvoiceStatus
    @Test
    void invoiceStatus_enumValuesAreCorrect() {
        // THEN : Vérification que les valeurs de l'énumération sont correctement
        // définies
        assertEquals("DRAFT", Invoice.InvoiceStatus.DRAFT.toString(), "Le statut DRAFT doit être correct.");
        assertEquals("FAILED", Invoice.InvoiceStatus.FAILED.toString(), "Le statut FAILED doit être correct.");
        assertEquals("PAID", Invoice.InvoiceStatus.PAID.toString(), "Le statut PAID doit être correct.");
        assertEquals("PENDING", Invoice.InvoiceStatus.PENDING.toString(), "Le statut PENDING doit être correct.");
        assertEquals("VOIDED", Invoice.InvoiceStatus.VOIDED.toString(), "Le statut VOIDED doit être correct.");
    }
}