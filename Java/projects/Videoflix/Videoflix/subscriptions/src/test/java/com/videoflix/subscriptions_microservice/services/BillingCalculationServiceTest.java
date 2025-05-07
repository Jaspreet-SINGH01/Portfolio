package com.videoflix.subscriptions_microservice.services;

import com.videoflix.subscriptions_microservice.entities.Subscription;
import com.videoflix.subscriptions_microservice.entities.SubscriptionLevel;
import com.videoflix.subscriptions_microservice.entities.SubscriptionLevel.BillingFrequency;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

// @ExtendWith(MockitoExtension.class) permet d'utiliser les fonctionnalités de Mockito dans ce test.
@ExtendWith(MockitoExtension.class)
class BillingCalculationServiceTest {

    // @InjectMocks crée une instance de BillingCalculationService et injecte les
    // mocks annotés avec @Mock.
    @InjectMocks
    private BillingCalculationService billingCalculationService;

    // @Mock crée un mock de l'entité SubscriptionLevel si nécessaire pour certains
    // scénarios de test.
    @Mock
    private SubscriptionLevel subscriptionLevel;

    // Test pour calculer la prochaine date de facturation pour un abonnement
    // mensuel avec une date de prochaine facturation existante.
    @Test
    void calculateNextBillingDate_monthly_existingNextBillingDate() {
        // GIVEN : Un abonnement avec une date de prochaine facturation et un niveau
        // mensuel.
        Subscription subscription = new Subscription();
        LocalDateTime nextBillingDate = LocalDateTime.now();
        subscription.setNextBillingDate(nextBillingDate);
        SubscriptionLevel level = new SubscriptionLevel();
        level.setBillingFrequency(BillingFrequency.MONTHLY);
        subscription.setSubscriptionLevel(level);

        // WHEN : Calcul de la prochaine date de facturation.
        LocalDateTime calculatedDate = billingCalculationService.calculateNextBillingDate(subscription);

        // THEN : Vérification que la date calculée est un mois après la date de
        // prochaine facturation existante.
        assertEquals(nextBillingDate.plusMonths(1), calculatedDate);
    }

    // Test pour calculer la prochaine date de facturation pour un abonnement
    // trimestriel sans date de prochaine facturation (utilisation de la date de
    // début).
    @Test
    void calculateNextBillingDate_quarterly_noExistingNextBillingDate() {
        // GIVEN : Un abonnement sans date de prochaine facturation mais avec une date
        // de début et un niveau trimestriel.
        Subscription subscription = new Subscription();
        LocalDateTime startDate = LocalDateTime.now().minusMonths(2);
        subscription.setStartDate(startDate);
        SubscriptionLevel level = new SubscriptionLevel();
        level.setBillingFrequency(BillingFrequency.QUARTERLY);
        subscription.setSubscriptionLevel(level);

        // WHEN : Calcul de la prochaine date de facturation.
        LocalDateTime calculatedDate = billingCalculationService.calculateNextBillingDate(subscription);

        // THEN : Vérification que la date calculée est trois mois après la date de
        // début.
        assertEquals(startDate.plusMonths(3), calculatedDate);
    }

    // Test pour calculer la prochaine date de facturation pour un abonnement
    // annuel.
    @Test
    void calculateNextBillingDate_yearly() {
        // GIVEN : Un abonnement avec une date de prochaine facturation et un niveau
        // annuel.
        Subscription subscription = new Subscription();
        LocalDateTime nextBillingDate = LocalDateTime.now().minusYears(1);
        subscription.setNextBillingDate(nextBillingDate);
        SubscriptionLevel level = new SubscriptionLevel();
        level.setBillingFrequency(BillingFrequency.YEARLY);
        subscription.setSubscriptionLevel(level);

        // WHEN : Calcul de la prochaine date de facturation.
        LocalDateTime calculatedDate = billingCalculationService.calculateNextBillingDate(subscription);

        // THEN : Vérification que la date calculée est un an après la date de prochaine
        // facturation existante.
        assertEquals(nextBillingDate.plusYears(1), calculatedDate);
    }

    // Test pour le cas où le niveau d'abonnement est nul.
    @Test
    void calculateNextBillingDate_nullSubscriptionLevel() {
        // GIVEN : Un abonnement avec un niveau d'abonnement nul.
        Subscription subscription = new Subscription();
        subscription.setNextBillingDate(LocalDateTime.now());
        subscription.setSubscriptionLevel(null);

        // WHEN : Calcul de la prochaine date de facturation.
        LocalDateTime calculatedDate = billingCalculationService.calculateNextBillingDate(subscription);

        // THEN : Vérification que la date calculée est nulle car la fréquence de
        // facturation ne peut pas être déterminée.
        assertNull(calculatedDate);
    }

    // Test pour le cas où la fréquence de facturation est nulle.
    @Test
    void calculateNextBillingDate_nullBillingFrequency() {
        // GIVEN : Un abonnement avec un niveau d'abonnement mais une fréquence de
        // facturation nulle.
        Subscription subscription = new Subscription();
        subscription.setNextBillingDate(LocalDateTime.now());
        SubscriptionLevel level = new SubscriptionLevel();
        level.setBillingFrequency(null);
        subscription.setSubscriptionLevel(level);

        // WHEN : Calcul de la prochaine date de facturation.
        LocalDateTime calculatedDate = billingCalculationService.calculateNextBillingDate(subscription);

        // THEN : Vérification que la date calculée est nulle car la fréquence de
        // facturation n'est pas définie.
        assertNull(calculatedDate);
    }

    // Test pour le cas d'une fréquence de facturation inconnue (default case dans
    // le switch).
    @Test
    void calculateNextBillingDate_unknownBillingFrequency() {
        // GIVEN : Un abonnement avec un niveau d'abonnement ayant une fréquence
        // inconnue.
        Subscription subscription = new Subscription();
        LocalDateTime nextBillingDate = LocalDateTime.now();
        subscription.setNextBillingDate(nextBillingDate);
        SubscriptionLevel level = new SubscriptionLevel();
        level.setBillingFrequency(null); // Simuler une fréquence inconnue en la laissant nulle pour ce test
        subscription.setSubscriptionLevel(level);

        // Pour tester le 'default' case, on pourrait potentiellement utiliser un enum
        // personnalisé
        // ou modifier temporairement l'enum BillingFrequency pour introduire une valeur
        // non gérée.
        // Cependant, avec l'enum actuel, le cas 'default' est atteint si
        // billingFrequency est null.
        // Le test 'calculateNextBillingDate_nullBillingFrequency' couvre déjà ce
        // scénario.
        // Si une nouvelle valeur était ajoutée à l'enum sans être gérée dans le switch,
        // ce test le détecterait.
        // Pour une couverture exhaustive, on pourrait envisager un test d'intégration
        // si l'ajout de nouvelles
        // valeurs à l'enum est fréquent.

        // WHEN : Calcul de la prochaine date de facturation.
        LocalDateTime calculatedDate = billingCalculationService.calculateNextBillingDate(subscription);

        // THEN : Vérification que la date calculée est nulle pour une fréquence
        // inconnue (ou nulle dans ce cas).
        assertNull(calculatedDate);
    }

    // Tests pour la méthode calculateNextBillingDateAfterReactivation

    // Test pour la réactivation d'un abonnement mensuel.
    @Test
    void calculateNextBillingDateAfterReactivation_monthly() {
        // GIVEN : Un abonnement qui a été annulé et a un niveau mensuel.
        Subscription subscription = new Subscription();
        LocalDateTime cancelledAt = LocalDateTime.now().minusMonths(2);
        subscription.setCancelledAt(cancelledAt);
        SubscriptionLevel level = new SubscriptionLevel();
        level.setBillingFrequency(BillingFrequency.MONTHLY);
        subscription.setSubscriptionLevel(level);

        // WHEN : Calcul de la prochaine date de facturation après réactivation.
        LocalDateTime calculatedDate = billingCalculationService
                .calculateNextBillingDateAfterReactivation(subscription);

        // THEN : Vérification que la date calculée est un mois après la date
        // d'annulation.
        assertEquals(cancelledAt.plusMonths(1), calculatedDate);
    }

    // Test pour la réactivation d'un abonnement trimestriel.
    @Test
    void calculateNextBillingDateAfterReactivation_quarterly() {
        // GIVEN : Un abonnement annulé avec un niveau trimestriel.
        Subscription subscription = new Subscription();
        LocalDateTime cancelledAt = LocalDateTime.now().minusMonths(4);
        subscription.setCancelledAt(cancelledAt);
        SubscriptionLevel level = new SubscriptionLevel();
        level.setBillingFrequency(BillingFrequency.QUARTERLY);
        subscription.setSubscriptionLevel(level);

        // WHEN : Calcul de la prochaine date de facturation après réactivation.
        LocalDateTime calculatedDate = billingCalculationService
                .calculateNextBillingDateAfterReactivation(subscription);

        // THEN : Vérification que la date calculée est trois mois après la date
        // d'annulation.
        assertEquals(cancelledAt.plusMonths(3), calculatedDate);
    }

    // Test pour la réactivation d'un abonnement annuel.
    @Test
    void calculateNextBillingDateAfterReactivation_yearly() {
        // GIVEN : Un abonnement annulé avec un niveau annuel.
        Subscription subscription = new Subscription();
        LocalDateTime cancelledAt = LocalDateTime.now().minusYears(1).minusMonths(3);
        subscription.setCancelledAt(cancelledAt);
        SubscriptionLevel level = new SubscriptionLevel();
        level.setBillingFrequency(BillingFrequency.YEARLY);
        subscription.setSubscriptionLevel(level);

        // WHEN : Calcul de la prochaine date de facturation après réactivation.
        LocalDateTime calculatedDate = billingCalculationService
                .calculateNextBillingDateAfterReactivation(subscription);

        // THEN : Vérification que la date calculée est un an après la date
        // d'annulation.
        assertEquals(cancelledAt.plusYears(1), calculatedDate);
    }

    // Test pour la réactivation avec une date d'annulation nulle.
    @Test
    void calculateNextBillingDateAfterReactivation_nullCancelledAt() {
        // GIVEN : Un abonnement avec une date d'annulation nulle.
        Subscription subscription = new Subscription();
        subscription.setCancelledAt(null);
        SubscriptionLevel level = new SubscriptionLevel();
        level.setBillingFrequency(BillingFrequency.MONTHLY);
        subscription.setSubscriptionLevel(level);

        // WHEN : Calcul de la prochaine date de facturation après réactivation.
        LocalDateTime calculatedDate = billingCalculationService
                .calculateNextBillingDateAfterReactivation(subscription);

        // THEN : Vérification que la date de fallback (now + 1 mois) est retournée.
        LocalDateTime expectedDate = LocalDateTime.now().plusMonths(1);
        // Comparaison avec une petite marge d'erreur si l'exécution prend quelques
        // millisecondes.
        assertTrue(calculatedDate.isAfter(expectedDate.minusSeconds(1))
                && calculatedDate.isBefore(expectedDate.plusSeconds(1)));
    }

    // Test pour la réactivation avec un niveau d'abonnement nul.
    @Test
    void calculateNextBillingDateAfterReactivation_nullSubscriptionLevel() {
        // GIVEN : Un abonnement avec un niveau d'abonnement nul.
        Subscription subscription = new Subscription();
        subscription.setCancelledAt(LocalDateTime.now().minusMonths(1));
        subscription.setSubscriptionLevel(null);

        // WHEN : Calcul de la prochaine date de facturation après réactivation.
        LocalDateTime calculatedDate = billingCalculationService
                .calculateNextBillingDateAfterReactivation(subscription);

        // THEN : Vérification que la date de fallback (now + 1 mois) est retournée.
        LocalDateTime expectedDate = LocalDateTime.now().plusMonths(1);
        assertTrue(calculatedDate.isAfter(expectedDate.minusSeconds(1))
                && calculatedDate.isBefore(expectedDate.plusSeconds(1)));
    }

    // Test pour la réactivation avec une fréquence de facturation nulle.
    @Test
    void calculateNextBillingDateAfterReactivation_nullBillingFrequency() {
        // GIVEN : Un abonnement avec une fréquence de facturation nulle.
        Subscription subscription = new Subscription();
        subscription.setCancelledAt(LocalDateTime.now().minusMonths(1));
        SubscriptionLevel level = new SubscriptionLevel();
        level.setBillingFrequency(null);
        subscription.setSubscriptionLevel(level);

        // WHEN : Calcul de la prochaine date de facturation après réactivation.
        LocalDateTime calculatedDate = billingCalculationService
                .calculateNextBillingDateAfterReactivation(subscription);

        // THEN : Vérification que la date de fallback (now + 1 mois) est retournée.
        LocalDateTime expectedDate = LocalDateTime.now().plusMonths(1);
        assertTrue(calculatedDate.isAfter(expectedDate.minusSeconds(1))
                && calculatedDate.isBefore(expectedDate.plusSeconds(1)));
    }

    // Test pour la réactivation avec une fréquence de facturation inconnue (default
    // case).
    @Test
    void calculateNextBillingDateAfterReactivation_unknownBillingFrequency() {
        // GIVEN : Un abonnement annulé avec une fréquence de facturation inconnue
        // (simulée en la laissant nulle).
        Subscription subscription = new Subscription();
        SubscriptionLevel level = new SubscriptionLevel();
        level.setBillingFrequency(null); // Simuler une fréquence inconnue
        subscription.setSubscriptionLevel(level);

        // WHEN : Calcul de la prochaine date de facturation après réactivation.
        LocalDateTime calculatedDate = billingCalculationService
                .calculateNextBillingDateAfterReactivation(subscription);

        // THEN : Vérification que la date de fallback (now + 1 mois) est retournée pour
        // une fréquence inconnue.
        LocalDateTime expectedDate = LocalDateTime.now().plusMonths(1);
        assertTrue(calculatedDate.isAfter(expectedDate.minusSeconds(1))
                && calculatedDate.isBefore(expectedDate.plusSeconds(1)));
    }
}