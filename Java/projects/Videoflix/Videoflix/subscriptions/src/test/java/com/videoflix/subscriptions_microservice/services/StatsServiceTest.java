package com.videoflix.subscriptions_microservice.services;

import com.videoflix.subscriptions_microservice.entities.Subscription;
import com.videoflix.subscriptions_microservice.entities.SubscriptionLevel;
import com.videoflix.subscriptions_microservice.repositories.SubscriptionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

// @ExtendWith(MockitoExtension.class) permet d'utiliser les fonctionnalités de Mockito dans ce test.
@ExtendWith(MockitoExtension.class)
class StatsServiceTest {

    // @Mock crée un mock du SubscriptionRepository.
    @Mock
    private SubscriptionRepository subscriptionRepository;

    // @InjectMocks crée une instance de StatsService et injecte le mock de
    // subscriptionRepository.
    @InjectMocks
    private StatsService statsService;

    // Test pour vérifier le calcul du nombre total d'abonnés actifs.
    @Test
    void getTotalActiveSubscribers_shouldReturnCorrectCount() {
        // GIVEN : Un nombre d'abonnés actifs simulé par le repository.
        long expectedCount = 150;
        when(subscriptionRepository.countByStatus(Subscription.SubscriptionStatus.ACTIVE)).thenReturn(expectedCount);

        // WHEN : Appel de la méthode getTotalActiveSubscribers.
        long actualCount = statsService.getTotalActiveSubscribers();

        // THEN : Vérification que le nombre retourné correspond à la valeur simulée.
        assertEquals(expectedCount, actualCount);
    }

    // Test pour vérifier le calcul du nombre de nouveaux abonnements sur une
    // période donnée.
    @Test
    void getNewSubscriptionsCount_shouldReturnCorrectCountForGivenPeriod() {
        // GIVEN : Une période de dates et un nombre de nouveaux abonnements simulé.
        LocalDate startDate = LocalDate.of(2025, 5, 1);
        LocalDate endDate = LocalDate.of(2025, 5, 10);
        long expectedCount = 50;
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();
        when(subscriptionRepository.countByStartDateBetween(startDateTime, endDateTime)).thenReturn(expectedCount);

        // WHEN : Appel de la méthode getNewSubscriptionsCount.
        long actualCount = statsService.getNewSubscriptionsCount(startDate, endDate);

        // THEN : Vérification que le nombre retourné correspond à la valeur simulée.
        assertEquals(expectedCount, actualCount);
    }

    // Test pour vérifier la répartition des abonnés par type d'abonnement.
    @Test
    void getSubscribersByType_shouldReturnCorrectDistribution() {
        // GIVEN : Une liste d'abonnements simulée avec différents niveaux.
        SubscriptionLevel basicLevel = new SubscriptionLevel();
        basicLevel.setId(1L);
        SubscriptionLevel premiumLevel = new SubscriptionLevel();
        premiumLevel.setId(2L);
        Subscription sub1 = new Subscription();
        sub1.setSubscriptionLevel(basicLevel);
        Subscription sub2 = new Subscription();
        sub2.setSubscriptionLevel(premiumLevel);
        Subscription sub3 = new Subscription();
        sub3.setSubscriptionLevel(basicLevel);
        List<Subscription> allSubscriptions = Arrays.asList(sub1, sub2, sub3);
        when(subscriptionRepository.findAll()).thenReturn(allSubscriptions);

        // WHEN : Appel de la méthode getSubscribersByType.
        Map<Object, Long> actualDistribution = statsService.getSubscribersByType();

        // THEN : Vérification que la répartition retournée est correcte.
        Map<Object, Long> expectedDistribution = new HashMap<>();
        expectedDistribution.put(1L, 2L); // 2 abonnés de niveau basic (ID 1)
        expectedDistribution.put(2L, 1L); // 1 abonné de niveau premium (ID 2)
        assertEquals(expectedDistribution, actualDistribution);
    }

    // Test pour vérifier le calcul du revenu total estimé sur une période donnée.
    @Test
    void getTotalRevenue_shouldReturnCorrectRevenueForGivenPeriod() {
        // GIVEN : Une période de dates et une liste d'abonnements actifs simulée avec
        // leurs niveaux et prix.
        LocalDate startDate = LocalDate.of(2025, 5, 1);
        LocalDate endDate = LocalDate.of(2025, 5, 10);
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();

        SubscriptionLevel basicLevel = new SubscriptionLevel();
        basicLevel.setPrice(4.99);
        SubscriptionLevel premiumLevel = new SubscriptionLevel();
        premiumLevel.setPrice(9.99);

        Subscription sub1 = new Subscription();
        sub1.setSubscriptionLevel(basicLevel);
        Subscription sub2 = new Subscription();
        sub2.setSubscriptionLevel(premiumLevel);

        List<Subscription> activeSubscriptions = Arrays.asList(sub1, sub2);
        when(subscriptionRepository.findByStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                Subscription.SubscriptionStatus.ACTIVE, endDateTime, startDateTime)).thenReturn(activeSubscriptions);

        // WHEN : Appel de la méthode getTotalRevenue.
        double actualRevenue = statsService.getTotalRevenue(startDate, endDate);

        // THEN : Vérification que le revenu total calculé est correct.
        double expectedRevenue = 4.99 + 9.99;
        assertEquals(expectedRevenue, actualRevenue, 0.001); // Utilisation d'une marge d'erreur pour les doubles
    }

    // Test pour vérifier le calcul du taux de rétention.
    @Test
    void getRetentionRate_shouldReturnCorrectRetentionRate() {
        // GIVEN : Une période de dates et des nombres d'abonnés actifs au début et à la
        // fin simulés.
        LocalDate startDate = LocalDate.of(2025, 4, 1);
        LocalDate endDate = LocalDate.of(2025, 4, 30);
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();
        long activeAtStart = 100;
        long activeAtEnd = 80;
        when(subscriptionRepository.countByStatusAndStartDateLessThan(Subscription.SubscriptionStatus.ACTIVE,
                startDateTime))
                .thenReturn(activeAtStart);
        when(subscriptionRepository.countByStatusAndEndDateGreaterThanEqual(Subscription.SubscriptionStatus.ACTIVE,
                endDateTime))
                .thenReturn(activeAtEnd);

        // WHEN : Appel de la méthode getRetentionRate.
        double actualRetentionRate = statsService.getRetentionRate(startDate, endDate);

        // THEN : Vérification que le taux de rétention calculé est correct.
        double expectedRetentionRate = (double) activeAtEnd / activeAtStart;
        assertEquals(expectedRetentionRate, actualRetentionRate, 0.001);
    }

    // Test pour vérifier le taux de rétention lorsque aucun abonné n'était actif au
    // début.
    @Test
    void getRetentionRate_shouldReturnZeroIfNoActiveSubscribersAtStart() {
        // GIVEN : Une période de dates où aucun abonné n'était actif au début.
        LocalDate startDate = LocalDate.of(2025, 4, 1);
        LocalDate endDate = LocalDate.of(2025, 4, 30);
        LocalDateTime startDateTime = startDate.atStartOfDay();
        when(subscriptionRepository.countByStatusAndStartDateLessThan(Subscription.SubscriptionStatus.ACTIVE,
                startDateTime))
                .thenReturn(0L);

        // WHEN : Appel de la méthode getRetentionRate.
        double actualRetentionRate = statsService.getRetentionRate(startDate, endDate);

        // THEN : Vérification que le taux de rétention est de zéro.
        assertEquals(0.0, actualRetentionRate, 0.001);
    }

    // Test pour vérifier le nombre d'abonnements avec le statut PAYMENT_FAILED.
    @Test
    void getFailedPaymentCount_shouldReturnCorrectCount() {
        // GIVEN : Un nombre d'abonnements en échec de paiement simulé.
        long expectedCount = 10;
        when(subscriptionRepository.countByStatus(Subscription.SubscriptionStatus.PAYMENT_FAILED))
                .thenReturn(expectedCount);

        // WHEN : Appel de la méthode getFailedPaymentCount.
        long actualCount = statsService.getFailedPaymentCount();

        // THEN : Vérification que le nombre retourné correspond à la valeur simulée.
        assertEquals(expectedCount, actualCount);
    }
}