package com.videoflix.subscriptions_microservice.tasks;

import com.videoflix.subscriptions_microservice.entities.Subscription;
import com.videoflix.subscriptions_microservice.repositories.SubscriptionRepository;
import com.videoflix.subscriptions_microservice.services.SubscriptionMetricsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

// @ExtendWith(MockitoExtension.class) permet d'initialiser les mocks automatiquement avant chaque test.
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests pour SubscriptionMetricsAggregationTask")
class SubscriptionMetricsAggregationTaskTest {

    // @Mock crée une instance mockée de SubscriptionRepository.
    // Nous allons simuler les comportements de comptage et de recherche d'abonnements.
    @Mock
    private SubscriptionRepository subscriptionRepository;

    // @Mock crée une instance mockée de SubscriptionMetricsService.
    // Nous allons vérifier que les métriques sont enregistrées correctement.
    @Mock
    private SubscriptionMetricsService metricsService;

    // @InjectMocks crée une instance de SubscriptionMetricsAggregationTask et y injecte les mocks.
    @InjectMocks
    private SubscriptionMetricsAggregationTask aggregationTask;

    // Horodatage fixe pour simuler 'LocalDateTime.now()' afin de rendre les tests déterministes.
    // La tâche est planifiée à 3h00 du matin.
    private LocalDateTime fixedNow;
    private LocalDateTime yesterday;
    private LocalDateTime startOfYesterday;
    private LocalDateTime endOfYesterday;

    /**
     * Méthode exécutée avant chaque test.
     * Initialise `fixedNow` et les bornes de temps pour les requêtes du repository.
     */
    @BeforeEach
    void setUp() {
        // Définit une date et heure fixe pour "aujourd'hui" (le 24 mai 2025 à 3h00 du matin)
        // cela correspond à l'heure d'exécution planifiée de la tâche.
        fixedNow = LocalDateTime.of(2025, 5, 24, 3, 0, 0);
        yesterday = fixedNow.minusDays(1);
        startOfYesterday = yesterday.toLocalDate().atStartOfDay(); // 23 mai 2025 00:00:00
        endOfYesterday = fixedNow.toLocalDate().atStartOfDay().minusSeconds(1); // 23 mai 2025 23:59:59

        // Réinitialise les mocks pour garantir l'indépendance des tests.
        reset(subscriptionRepository, metricsService);
    }

    @Test
    @DisplayName("Devrait agréger et enregistrer toutes les métriques journalières avec succès")
    void aggregateDailySubscriptionMetrics_shouldAggregateAllMetricsSuccessfully() {
        // GIVEN: Des données simulées pour les abonnements.
        long newSubscriptionsCount = 5L;
        long cancellationsCount = 2L;
        
        // Simule des abonnements actifs pour le calcul des revenus
        Subscription activeSub1 = new Subscription();
        activeSub1.setId(1L);
        activeSub1.setPriceId("10.0"); // Prix de l'abonnement
        activeSub1.setStatus(Subscription.SubscriptionStatus.ACTIVE);
        activeSub1.setPaymentDate(startOfYesterday.plusHours(1)); // Payé hier

        Subscription activeSub2 = new Subscription();
        activeSub2.setId(2L);
        activeSub2.setPriceId("15.50");
        activeSub2.setStatus(Subscription.SubscriptionStatus.ACTIVE);
        activeSub2.setPaymentDate(startOfYesterday.plusHours(12)); // Payé hier

        List<Subscription> activeSubscriptions = Arrays.asList(activeSub1, activeSub2);
        double expectedDailyRevenue = 10.0 + 15.50; // Somme des prix des abonnements

        long activeSubscriptionsEndYesterdayCount = 100L;

        // Configure le comportement des mocks:
        // Pour les nouveaux abonnements
        when(subscriptionRepository.countByCreationTimestampBetween(startOfYesterday, endOfYesterday))
                .thenReturn(newSubscriptionsCount);
        // Pour les annulations
        when(subscriptionRepository.countByStatusAndCancellationDateBetween(
                Subscription.SubscriptionStatus.CANCELLED, startOfYesterday, endOfYesterday))
                .thenReturn(cancellationsCount);
        // Pour les revenus (abonnements actifs payés hier)
        when(subscriptionRepository.findByStatusAndLastPaymentDateBetween(
                Subscription.SubscriptionStatus.ACTIVE, any(LocalDateTime.class), endOfYesterday)) // La date de début est fixedNow.minusDays(31), on utilise any() pour la flexibilité
                .thenReturn(activeSubscriptions);
        // Pour le nombre d'abonnements actifs en fin de journée
        when(subscriptionRepository.countByStatus(Subscription.SubscriptionStatus.ACTIVE))
                .thenReturn(activeSubscriptionsEndYesterdayCount);

        // WHEN: La tâche d'agrégation est exécutée.
        aggregationTask.aggregateDailySubscriptionMetrics();

        // THEN:
        // Vérifie les appels aux méthodes du SubscriptionRepository avec les bons paramètres.
        verify(subscriptionRepository, times(1)).countByCreationTimestampBetween(startOfYesterday, endOfYesterday);
        verify(subscriptionRepository, times(1)).countByStatusAndCancellationDateBetween(
                Subscription.SubscriptionStatus.CANCELLED, startOfYesterday, endOfYesterday);
        verify(subscriptionRepository, times(1)).findByStatusAndLastPaymentDateBetween(
                Subscription.SubscriptionStatus.ACTIVE, any(LocalDateTime.class), endOfYesterday);
        verify(subscriptionRepository, times(1)).countByStatus(Subscription.SubscriptionStatus.ACTIVE);

        // Vérifie que les métriques ont été enregistrées dans le SubscriptionMetricsService.
        verify(metricsService, times(1)).recordDailyMetric("new_subscriptions", yesterday, newSubscriptionsCount);
        verify(metricsService, times(1)).recordDailyMetric("cancelled_subscriptions", yesterday, cancellationsCount);
        verify(metricsService, times(1)).recordDailyMetric("daily_revenue", yesterday, expectedDailyRevenue);
        verify(metricsService, times(1)).recordDailyMetric("active_subscriptions_end", yesterday, activeSubscriptionsEndYesterdayCount);
    }

    @Test
    @DisplayName("Devrait gérer le cas où il n'y a pas de nouveaux abonnements ou annulations")
    void aggregateDailySubscriptionMetrics_shouldHandleZeroCounts() {
        // GIVEN: Zéro nouveaux abonnements et zéro annulations.
        long newSubscriptionsCount = 0L;
        long cancellationsCount = 0L;
        
        // Simule qu'aucun abonnement actif n'a payé hier
        List<Subscription> noActiveSubscriptions = Collections.emptyList();
        double expectedDailyRevenue = 0.0;

        long activeSubscriptionsEndYesterdayCount = 80L; // Exemple de nombre d'actifs

        // Configure le comportement des mocks pour retourner zéro ou des listes vides.
        when(subscriptionRepository.countByCreationTimestampBetween(any(), any())).thenReturn(newSubscriptionsCount);
        when(subscriptionRepository.countByStatusAndCancellationDateBetween(any(), any(), any())).thenReturn(cancellationsCount);
        when(subscriptionRepository.findByStatusAndLastPaymentDateBetween(any(), any(), any())).thenReturn(noActiveSubscriptions);
        when(subscriptionRepository.countByStatus(any())).thenReturn(activeSubscriptionsEndYesterdayCount);

        // WHEN: La tâche est exécutée.
        aggregationTask.aggregateDailySubscriptionMetrics();

        // THEN:
        // Vérifie que toutes les méthodes du repository ont été appelées.
        verify(subscriptionRepository, times(1)).countByCreationTimestampBetween(startOfYesterday, endOfYesterday);
        verify(subscriptionRepository, times(1)).countByStatusAndCancellationDateBetween(
                Subscription.SubscriptionStatus.CANCELLED, startOfYesterday, endOfYesterday);
        verify(subscriptionRepository, times(1)).findByStatusAndLastPaymentDateBetween(
                Subscription.SubscriptionStatus.ACTIVE, any(LocalDateTime.class), endOfYesterday);
        verify(subscriptionRepository, times(1)).countByStatus(Subscription.SubscriptionStatus.ACTIVE);

        // Vérifie que les métriques ont été enregistrées, y compris avec des valeurs de zéro.
        verify(metricsService, times(1)).recordDailyMetric("new_subscriptions", yesterday, newSubscriptionsCount);
        verify(metricsService, times(1)).recordDailyMetric("cancelled_subscriptions", yesterday, cancellationsCount);
        verify(metricsService, times(1)).recordDailyMetric("daily_revenue", yesterday, expectedDailyRevenue);
        verify(metricsService, times(1)).recordDailyMetric("active_subscriptions_end", yesterday, activeSubscriptionsEndYesterdayCount);
    }

    @Test
    @DisplayName("Devrait gérer les prix invalides pour le calcul des revenus et les ignorer")
    void aggregateDailySubscriptionMetrics_shouldHandleInvalidPricesForRevenue() {
        // GIVEN: Un abonnement avec un `priceId` invalide (non-numérique).
        long newSubscriptionsCount = 1L;
        long cancellationsCount = 0L;

        Subscription activeSubValidPrice = new Subscription();
        activeSubValidPrice.setId(1L);
        activeSubValidPrice.setPriceId("20.0");
        activeSubValidPrice.setStatus(Subscription.SubscriptionStatus.ACTIVE);
        activeSubValidPrice.setPaymentDate(startOfYesterday.plusHours(1));

        Subscription activeSubInvalidPrice = new Subscription();
        activeSubInvalidPrice.setId(2L);
        activeSubInvalidPrice.setPriceId("INVALID_PRICE"); // Prix invalide
        activeSubInvalidPrice.setStatus(Subscription.SubscriptionStatus.ACTIVE);
        activeSubInvalidPrice.setPaymentDate(startOfYesterday.plusHours(2));

        List<Subscription> activeSubscriptions = Arrays.asList(activeSubValidPrice, activeSubInvalidPrice);
        double expectedDailyRevenue = 20.0; // Seul le prix valide est compté

        long activeSubscriptionsEndYesterdayCount = 90L;

        // Configure le comportement des mocks.
        when(subscriptionRepository.countByCreationTimestampBetween(any(), any())).thenReturn(newSubscriptionsCount);
        when(subscriptionRepository.countByStatusAndCancellationDateBetween(any(), any(), any())).thenReturn(cancellationsCount);
        when(subscriptionRepository.findByStatusAndLastPaymentDateBetween(any(), any(), any())).thenReturn(activeSubscriptions);
        when(subscriptionRepository.countByStatus(any())).thenReturn(activeSubscriptionsEndYesterdayCount);

        // WHEN: La tâche est exécutée.
        aggregationTask.aggregateDailySubscriptionMetrics();

        // THEN:
        // Vérifie que les appels aux méthodes du repository sont corrects.
        verify(subscriptionRepository, times(1)).findByStatusAndLastPaymentDateBetween(
                eq(Subscription.SubscriptionStatus.ACTIVE), any(LocalDateTime.class), eq(endOfYesterday));

        // Vérifie que le revenu enregistré est correct, ignorant le prix invalide.
        verify(metricsService, times(1)).recordDailyMetric("daily_revenue", yesterday, expectedDailyRevenue);
        // (Il serait également possible de vérifier les logs ici pour confirmer l'avertissement de prix invalide.)
    }

    @Test
    @DisplayName("Devrait gérer une exception lors de l'agrégation et loguer l'erreur")
    void aggregateDailySubscriptionMetrics_shouldHandleExceptionAndLogError() {
        // GIVEN: Une exception se produit lors de l'appel au repository pour les nouveaux abonnements.
        String errorMessage = "Erreur simulée lors du comptage des nouveaux abonnements";
        doThrow(new RuntimeException(errorMessage))
                .when(subscriptionRepository).countByCreationTimestampBetween(any(), any());

        // WHEN: La tâche est exécutée.
        aggregationTask.aggregateDailySubscriptionMetrics();

        // THEN:
        // Vérifie que la méthode qui a causé l'erreur a été appelée.
        verify(subscriptionRepository, times(1)).countByCreationTimestampBetween(startOfYesterday, endOfYesterday);
        
        // Vérifie qu'aucune autre méthode du repository ou du service de métriques n'a été appelée après l'erreur.
        verify(subscriptionRepository, never()).countByStatusAndCancellationDateBetween(any(), any(), any());
        verify(metricsService, never()).recordDailyMetric(anyString(), any(LocalDateTime.class), anyDouble());
        verify(metricsService, never()).recordDailyMetric(anyString(), any(LocalDateTime.class), anyLong());
        // (Vérifier les logs serait également pertinent ici pour confirmer l'enregistrement de l'erreur.)
    }
}