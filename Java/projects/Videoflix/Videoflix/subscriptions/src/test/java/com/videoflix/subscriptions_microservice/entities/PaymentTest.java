package com.videoflix.subscriptions_microservice.entities;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PaymentTest {

    // Test pour vérifier la création et la récupération des attributs d'un paiement
    // réussi
    @Test
    void payment_successfulPayment_shouldSetAndGetValues() {
        // GIVEN : Création des valeurs nécessaires pour un paiement réussi
        Subscription mockSubscription = Mockito.mock(Subscription.class); // Mock de l'entité Subscription associée
        LocalDateTime paymentDate = LocalDateTime.now();
        double amount = 9.99;
        String paymentId = "stripe_pay_123";
        Payment.PaymentStatus status = Payment.PaymentStatus.SUCCESS;
        String errorMessage = null; // Pas d'erreur pour un paiement réussi

        // WHEN : Création d'une instance de Payment et définition de ses attributs
        Payment payment = new Payment();
        payment.setSubscription(mockSubscription);
        payment.setPaymentDate(paymentDate);
        payment.setAmount(amount);
        payment.setPaymentId(paymentId);
        payment.setStatus(status);
        payment.setErrorMessage(errorMessage);

        // THEN : Vérification que les valeurs ont été correctement définies et peuvent
        // être récupérées
        assertNull(payment.getId(), "L'ID devrait être null avant la persistance.");
        assertEquals(mockSubscription, payment.getSubscription(), "L'abonnement associé doit correspondre.");
        assertEquals(paymentDate, payment.getPaymentDate(), "La date de paiement doit correspondre.");
        assertEquals(amount, payment.getAmount(), "Le montant du paiement doit correspondre.");
        assertEquals(paymentId, payment.getPaymentId(), "L'identifiant du paiement doit correspondre.");
        assertEquals(status, payment.getStatus(), "Le statut du paiement doit être SUCCESS.");
        assertNull(payment.getErrorMessage(), "Le message d'erreur devrait être null pour un paiement réussi.");
    }

    // Test pour vérifier la création et la récupération des attributs d'un paiement
    // échoué
    @Test
    void payment_failedPayment_shouldSetAndGetValues() {
        // GIVEN : Création des valeurs nécessaires pour un paiement échoué
        Subscription mockSubscription = Mockito.mock(Subscription.class); // Mock de l'entité Subscription associée
        LocalDateTime paymentDate = LocalDateTime.now();
        double amount = 19.99;
        String paymentId = "stripe_pay_fail_456";
        Payment.PaymentStatus status = Payment.PaymentStatus.FAILED;
        String errorMessage = "Payment declined by gateway.";

        // WHEN : Création d'une instance de Payment et définition de ses attributs
        Payment payment = new Payment();
        payment.setSubscription(mockSubscription);
        payment.setPaymentDate(paymentDate);
        payment.setAmount(amount);
        payment.setPaymentId(paymentId);
        payment.setStatus(status);
        payment.setErrorMessage(errorMessage);

        // THEN : Vérification que les valeurs ont été correctement définies et peuvent
        // être récupérées
        assertNull(payment.getId(), "L'ID devrait être null avant la persistance.");
        assertEquals(mockSubscription, payment.getSubscription(), "L'abonnement associé doit correspondre.");
        assertEquals(paymentDate, payment.getPaymentDate(), "La date de paiement doit correspondre.");
        assertEquals(amount, payment.getAmount(), "Le montant du paiement doit correspondre.");
        assertEquals(paymentId, payment.getPaymentId(), "L'identifiant du paiement doit correspondre.");
        assertEquals(status, payment.getStatus(), "Le statut du paiement doit être FAILED.");
        assertEquals(errorMessage, payment.getErrorMessage(), "Le message d'erreur doit correspondre.");
    }

    // Test pour vérifier la création et la récupération des attributs d'un paiement
    // en attente
    @Test
    void payment_pendingPayment_shouldSetAndGetValues() {
        // GIVEN : Création des valeurs nécessaires pour un paiement en attente
        Subscription mockSubscription = Mockito.mock(Subscription.class); // Mock de l'entité Subscription associée
        LocalDateTime paymentDate = LocalDateTime.now();
        double amount = 5.99;
        String paymentId = "paypal_pending_789";
        Payment.PaymentStatus status = Payment.PaymentStatus.PENDING;
        String errorMessage = null; // Pas d'erreur initialement pour un paiement en attente

        // WHEN : Création d'une instance de Payment et définition de ses attributs
        Payment payment = new Payment();
        payment.setSubscription(mockSubscription);
        payment.setPaymentDate(paymentDate);
        payment.setAmount(amount);
        payment.setPaymentId(paymentId);
        payment.setStatus(status);
        payment.setErrorMessage(errorMessage);

        // THEN : Vérification que les valeurs ont été correctement définies et peuvent
        // être récupérées
        assertNull(payment.getId(), "L'ID devrait être null avant la persistance.");
        assertEquals(mockSubscription, payment.getSubscription(), "L'abonnement associé doit correspondre.");
        assertEquals(paymentDate, payment.getPaymentDate(), "La date de paiement doit correspondre.");
        assertEquals(amount, payment.getAmount(), "Le montant du paiement doit correspondre.");
        assertEquals(paymentId, payment.getPaymentId(), "L'identifiant du paiement doit correspondre.");
        assertEquals(status, payment.getStatus(), "Le statut du paiement doit être PENDING.");
        assertNull(payment.getErrorMessage(),
                "Le message d'erreur devrait être null initialement pour un paiement en attente.");
    }

    // Test pour vérifier l'énumération PaymentStatus
    @Test
    void paymentStatus_enumValuesAreCorrect() {
        // THEN : Vérification que les valeurs de l'énumération sont correctement
        // définies
        assertEquals("SUCCESS", Payment.PaymentStatus.SUCCESS.toString(), "Le statut SUCCESS doit être correct.");
        assertEquals("FAILED", Payment.PaymentStatus.FAILED.toString(), "Le statut FAILED doit être correct.");
        assertEquals("PENDING", Payment.PaymentStatus.PENDING.toString(), "Le statut PENDING doit être correct.");
    }
}