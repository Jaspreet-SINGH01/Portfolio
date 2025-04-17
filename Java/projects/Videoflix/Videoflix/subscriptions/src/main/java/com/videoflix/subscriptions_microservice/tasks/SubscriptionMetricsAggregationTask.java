package com.videoflix.subscriptions_microservice.tasks;

import com.videoflix.subscriptions_microservice.entities.Subscription;
import com.videoflix.subscriptions_microservice.repositories.SubscriptionRepository;
import com.videoflix.subscriptions_microservice.services.SubscriptionMetricsService; // Service pour agréger et stocker les métriques
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class SubscriptionMetricsAggregationTask {

    private static final Logger logger = LoggerFactory.getLogger(SubscriptionMetricsAggregationTask.class);

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionMetricsService metricsService;

    public SubscriptionMetricsAggregationTask(SubscriptionRepository subscriptionRepository,
            SubscriptionMetricsService metricsService) {
        this.subscriptionRepository = subscriptionRepository;
        this.metricsService = metricsService;
    }

    // Planification de l'exécution de cette tâche tous les jours à 3h00 du matin
    @Scheduled(cron = "0 0 3 * * *")
    public void aggregateDailySubscriptionMetrics() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDateTime startOfYesterday = yesterday.atStartOfDay();
        LocalDateTime endOfYesterday = today.atStartOfDay();

        logger.info("Début de l'agrégation des métriques d'abonnement pour le {}...", yesterday);

        try {
            // Nombre de nouveaux abonnements hier
            long newSubscriptions = subscriptionRepository.countByCreationTimestampBetween(startOfYesterday,
                    endOfYesterday);
            metricsService.recordDailyMetric("new_subscriptions", yesterday, newSubscriptions);
            logger.info("Nombre de nouveaux abonnements pour le {} : {}", yesterday, newSubscriptions);

            // Nombre d'annulations hier
            long cancellations = subscriptionRepository.countByStatusAndCancellationDateBetween(
                    Subscription.SubscriptionStatus.CANCELLED, startOfYesterday.toLocalDate(),
                    endOfYesterday.toLocalDate());
            metricsService.recordDailyMetric("cancelled_subscriptions", yesterday, cancellations);
            logger.info("Nombre d'annulations pour le {} : {}", yesterday, cancellations);

            // Revenus générés hier
            List<Subscription> activeSubscriptionsYesterday = subscriptionRepository
                    .findByStatusAndLastPaymentDateBetween(Subscription.SubscriptionStatus.ACTIVE,
                            startOfYesterday.toLocalDate().minusDays(31), endOfYesterday.toLocalDate()); // Ajuster la
                                                                                                         // période si
                                                                                                         // nécessaire
            double dailyRevenue = activeSubscriptionsYesterday.stream()
                    .filter(sub -> sub.getPriceId() != null)
                    .mapToDouble(sub -> {
                        try {
                            return Double.parseDouble(sub.getPriceId());
                        } catch (NumberFormatException e) {
                            logger.warn("Prix invalide pour l'abonnement {}: {}", sub.getId(), sub.getPriceId());
                            return 0.0;
                        }
                    })
                    .sum();
            metricsService.recordDailyMetric("daily_revenue", yesterday, dailyRevenue);
            logger.info("Revenus générés pour le {} : {}", yesterday, dailyRevenue);

            // Taux d'annulation
            long activeSubscriptionsEndYesterday = subscriptionRepository
                    .countByStatus(Subscription.SubscriptionStatus.ACTIVE);
            metricsService.recordDailyMetric("active_subscriptions_end", yesterday, activeSubscriptionsEndYesterday);
            logger.info("Nombre d'abonnements actifs à la fin du {} : {}", yesterday, activeSubscriptionsEndYesterday);

            logger.info("Agrégation des métriques d'abonnement pour le {} terminée.", yesterday);

        } catch (Exception e) {
            logger.error("Erreur lors de l'agrégation des métriques d'abonnement pour le {} : {}", yesterday,
                    e.getMessage(), e);
        }
    }
}