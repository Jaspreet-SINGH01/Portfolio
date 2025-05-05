package com.videoflix.subscriptions_microservice.entities;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SubscriptionTest {

    // Test pour vérifier la création et la récupération des attributs d'un
    // abonnement
    @Test
    void subscription_shouldSetAndGetValues() {
        // GIVEN : Création d'objets mockés pour les relations ManyToOne
        User mockUser = Mockito.mock(User.class);
        SubscriptionLevel mockSubscriptionLevel = Mockito.mock(SubscriptionLevel.class);
        Promotion mockPromotion = Mockito.mock(Promotion.class);
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = LocalDateTime.now().plusMonths(1);
        Subscription.SubscriptionStatus status = Subscription.SubscriptionStatus.ACTIVE;
        boolean autoRenew = true;
        LocalDateTime paymentDate = LocalDateTime.now().minusDays(1);
        String paymentId = "pay_123";
        LocalDateTime nextRenewalDate = LocalDateTime.now().plusWeeks(3);
        LocalDateTime nextBillingDate = LocalDateTime.now().plusWeeks(3);
        LocalDateTime nextRetryDate = LocalDateTime.now().plusDays(1);
        String customerId = "cust_456";
        String stripeSubscriptionId = "sub_789";
        String priceId = "price_abc";
        double price = 9.99;
        String currency = "EUR";
        String lastPaymentError = null;
        LocalDateTime trialStartDate = LocalDateTime.now().minusDays(7);
        LocalDateTime trialEndDate = LocalDateTime.now().plusDays(7);
        LocalDateTime refundDate = null;
        double refundAmount = 0.0;
        LocalDateTime cancelledAt = null;
        String stripeChargeId = null;

        // WHEN : Création d'une instance de Subscription et définition de ses attributs
        Subscription subscription = new Subscription();
        subscription.setUser(mockUser);
        subscription.setSubscriptionLevel(mockSubscriptionLevel);
        subscription.setPromotion(mockPromotion);
        subscription.setStartDate(startDate);
        subscription.setEndDate(endDate);
        subscription.setStatus(status);
        subscription.setAutoRenew(autoRenew);
        subscription.setPaymentDate(paymentDate);
        subscription.setPaymentId(paymentId);
        subscription.setNextRenewalDate(nextRenewalDate);
        subscription.setNextBillingDate(nextBillingDate);
        subscription.setNextRetryDate(nextRetryDate);
        subscription.setCustomerId(customerId);
        subscription.setStripeSubscriptionId(stripeSubscriptionId);
        subscription.setPriceId(priceId);
        subscription.setPrice(price);
        subscription.setCurrency(currency);
        subscription.setLastPaymentError(lastPaymentError);
        subscription.setTrialStartDate(trialStartDate);
        subscription.setTrialEndDate(trialEndDate);
        subscription.setRefundDate(refundDate);
        subscription.setRefundAmount(refundAmount);
        subscription.setCancelledAt(cancelledAt);
        subscription.setStripeChargeId(stripeChargeId);

        // THEN : Vérification que les valeurs ont été correctement définies et peuvent
        // être récupérées
        assertNull(subscription.getId(), "L'ID devrait être null avant la persistance.");
        assertEquals(mockUser, subscription.getUser(), "L'utilisateur doit correspondre.");
        assertEquals(mockSubscriptionLevel, subscription.getSubscriptionLevel(),
                "Le niveau d'abonnement doit correspondre.");
        assertEquals(mockPromotion, subscription.getPromotion(), "La promotion doit correspondre.");
        assertEquals(startDate, subscription.getStartDate(), "La date de début doit correspondre.");
        assertEquals(endDate, subscription.getEndDate(), "La date de fin doit correspondre.");
        assertEquals(status, subscription.getStatus(), "Le statut doit correspondre.");
        assertEquals(autoRenew, subscription.isAutoRenew(), "Le renouvellement automatique doit correspondre.");
        assertEquals(paymentDate, subscription.getPaymentDate(), "La date de paiement doit correspondre.");
        assertEquals(paymentId, subscription.getPaymentId(), "L'ID de paiement doit correspondre.");
        assertEquals(nextRenewalDate, subscription.getNextRenewalDate(),
                "La date de prochain renouvellement doit correspondre.");
        assertEquals(nextBillingDate, subscription.getNextBillingDate(),
                "La date de prochaine facturation doit correspondre.");
        assertEquals(nextRetryDate, subscription.getNextRetryDate(),
                "La date de prochaine tentative doit correspondre.");
        assertEquals(customerId, subscription.getCustomerId(), "L'ID client doit correspondre.");
        assertEquals(stripeSubscriptionId, subscription.getStripeSubscriptionId(),
                "L'ID d'abonnement Stripe doit correspondre.");
        assertEquals(priceId, subscription.getPriceId(), "L'ID de prix doit correspondre.");
        assertEquals(price, subscription.getPrice(), "Le prix doit correspondre.");
        assertEquals(currency, subscription.getCurrency(), "La devise doit correspondre.");
        assertEquals(lastPaymentError, subscription.getLastPaymentError(),
                "Le message d'erreur du dernier paiement doit correspondre.");
        assertEquals(trialStartDate, subscription.getTrialStartDate(),
                "La date de début de l'essai doit correspondre.");
        assertEquals(trialEndDate, subscription.getTrialEndDate(), "La date de fin de l'essai doit correspondre.");
        assertEquals(refundDate, subscription.getRefundDate(), "La date de remboursement doit correspondre.");
        assertEquals(refundAmount, subscription.getRefundAmount(), "Le montant du remboursement doit correspondre.");
        assertEquals(cancelledAt, subscription.getCancelledAt(), "La date d'annulation doit correspondre.");
        assertEquals(stripeChargeId, subscription.getStripeChargeId(), "L'ID de charge Stripe doit correspondre.");
    }

    // Test pour vérifier le fonctionnement de la méthode getNextBillingDate pour un
    // abonnement actif avec renouvellement automatique
    @Test
    void getNextBillingDate_activeAutoRenew_shouldReturnNextRenewalDate() {
        // GIVEN : Un abonnement actif avec renouvellement automatique et une date de
        // prochain renouvellement définie
        Subscription subscription = new Subscription();
        subscription.setStatus(Subscription.SubscriptionStatus.ACTIVE);
        subscription.setAutoRenew(true);
        LocalDateTime nextRenewalDate = LocalDateTime.now().plusWeeks(2);
        subscription.setNextRenewalDate(nextRenewalDate);

        // WHEN : Appel de la méthode getNextBillingDate
        LocalDateTime billingDate = subscription.getNextBillingDate();

        // THEN : La méthode devrait retourner la date de prochain renouvellement
        assertEquals(nextRenewalDate, billingDate,
                "La date de prochaine facturation doit être la date de prochain renouvellement.");
    }

    // Test pour vérifier le fonctionnement de getNextBillingDate pour un abonnement
    // en période d'essai
    @Test
    void getNextBillingDate_trialSubscription_shouldReturnTrialEndDate() {
        // GIVEN : Un abonnement en période d'essai avec une date de fin d'essai définie
        Subscription subscription = new Subscription();
        subscription.setStatus(Subscription.SubscriptionStatus.TRIAL);
        LocalDateTime trialEndDate = LocalDateTime.now().plusDays(5);
        subscription.setTrialEndDate(trialEndDate);
        subscription.setAutoRenew(true); // Le renouvellement automatique n'affecte pas directement la date de fin
                                         // d'essai

        // WHEN : Appel de la méthode getNextBillingDate
        LocalDateTime billingDate = subscription.getNextBillingDate();

        // THEN : La méthode devrait retourner la date de fin d'essai
        assertEquals(trialEndDate, billingDate, "La date de prochaine facturation doit être la date de fin d'essai.");
    }

    // Test pour vérifier le fonctionnement de getNextBillingDate pour un abonnement
    // inactif
    @Test
    void getNextBillingDate_inactiveSubscription_shouldReturnNull() {
        // GIVEN : Un abonnement inactif
        Subscription subscription = new Subscription();
        subscription.setStatus(Subscription.SubscriptionStatus.INACTIVE);
        subscription.setAutoRenew(true);
        subscription.setNextRenewalDate(LocalDateTime.now().plusWeeks(1)); // Date de renouvellement sans importance

        // WHEN : Appel de la méthode getNextBillingDate
        LocalDateTime billingDate = subscription.getNextBillingDate();

        // THEN : La méthode devrait retourner null
        assertNull(billingDate, "La date de prochaine facturation devrait être null pour un abonnement inactif.");
    }

    // Test pour vérifier le fonctionnement de getNextBillingDate pour un abonnement
    // actif sans renouvellement automatique
    @Test
    void getNextBillingDate_activeNoAutoRenew_shouldReturnNull() {
        // GIVEN : Un abonnement actif mais sans renouvellement automatique
        Subscription subscription = new Subscription();
        subscription.setStatus(Subscription.SubscriptionStatus.ACTIVE);
        subscription.setAutoRenew(false);
        subscription.setNextRenewalDate(LocalDateTime.now().plusWeeks(1)); // Date de renouvellement sans importance

        // WHEN : Appel de la méthode getNextBillingDate
        LocalDateTime billingDate = subscription.getNextBillingDate();

        // THEN : La méthode devrait retourner null
        assertNull(billingDate,
                "La date de prochaine facturation devrait être null si le renouvellement automatique est désactivé.");
    }

    // Test pour vérifier l'énumération SubscriptionStatus
    @Test
    void subscriptionStatus_enumValuesAreCorrect() {
        // THEN : Vérification que les valeurs de l'énumération sont correctement
        // définies
        assertEquals("ACTIVE", Subscription.SubscriptionStatus.ACTIVE.toString(),
                "Le statut ACTIVE doit être correct.");
        assertEquals("ARCHIVED", Subscription.SubscriptionStatus.ARCHIVED.toString(),
                "Le statut ARCHIVED doit être correct.");
        assertEquals("CANCELLED", Subscription.SubscriptionStatus.CANCELLED.toString(),
                "Le statut CANCELLED doit être correct.");
        assertEquals("EXPIRED", Subscription.SubscriptionStatus.EXPIRED.toString(),
                "Le statut EXPIRED doit être correct.");
        assertEquals("INACTIVE", Subscription.SubscriptionStatus.INACTIVE.toString(),
                "Le statut INACTIVE doit être correct.");
        assertEquals("PENDING", Subscription.SubscriptionStatus.PENDING.toString(),
                "Le statut PENDING doit être correct.");
        assertEquals("PAYMENT_FAILED", Subscription.SubscriptionStatus.PAYMENT_FAILED.toString(),
                "Le statut PAYMENT_FAILED doit être correct.");
        assertEquals("TRIAL", Subscription.SubscriptionStatus.TRIAL.toString(), "Le statut TRIAL doit être correct.");
        assertEquals("TRIAL_ENDED", Subscription.SubscriptionStatus.TRIAL_ENDED.toString(),
                "Le statut TRIAL_ENDED doit être correct.");
    }

    // Test pour vérifier la relation OneToMany avec l'entité Payment
    @Test
    void subscription_oneToManyRelationWithPayments() {
        // GIVEN : Création d'une instance de Subscription et de deux instances de
        // Payment mockées
        Subscription subscription = new Subscription();
        Payment payment1 = Mockito.mock(Payment.class);
        Payment payment2 = Mockito.mock(Payment.class);
        List<Payment> payments = Arrays.asList(payment1, payment2);

        // WHEN : Association des paiements à l'abonnement
        subscription.setPayments(payments);

        // THEN : Vérification que la liste des paiements a été correctement définie
        assertEquals(2, subscription.getPayments().size(), "La liste des paiements doit contenir deux éléments.");
        assertEquals(payment1, subscription.getPayments().get(0), "Le premier paiement doit correspondre.");
        assertEquals(payment2, subscription.getPayments().get(1), "Le deuxième paiement doit correspondre.");
    }

    // Test pour vérifier que la liste des paiements est initialement vide si elle
    // n'est pas définie
    @Test
    void subscription_paymentsShouldBeEmptyByDefault() {
        // GIVEN : Création d'une instance de Subscription
        Subscription subscription = new Subscription();

        // THEN : Vérification que la liste des paiements est initialement vide
        assertEquals(0, subscription.getPayments().size(), "La liste des paiements devrait être vide par défaut.");
    }
}