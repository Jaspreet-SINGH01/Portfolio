package com.videoflix.subscriptions_microservice.services;

import com.videoflix.subscriptions_microservice.entities.Subscription;
import com.videoflix.subscriptions_microservice.entities.SubscriptionLevel;
import com.videoflix.subscriptions_microservice.repositories.SubscriptionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StatsService {

    private final SubscriptionRepository subscriptionRepository;

    public StatsService(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    // Nombre total d'abonnés actifs
    public long getTotalActiveSubscribers() {
        return subscriptionRepository.countByStatus(Subscription.SubscriptionStatus.ACTIVE);
    }

    // Nombre de nouveaux abonnements sur une période donnée
    public long getNewSubscriptionsCount(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();
        return subscriptionRepository.countByStartDateBetween(startDateTime, endDateTime);
    }

    // Répartition des abonnés par type d'abonnement
    public Map<Object, Long> getSubscribersByType() {
        return subscriptionRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        subscription -> subscription.getSubscriptionLevel().getId(),
                        Collectors.counting()));
    }

    // Revenu total estimé sur une période donnée (nécessite une logique de prix)
    public double getTotalRevenue(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();
        List<Subscription> activeSubscriptions = subscriptionRepository
                .findByStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        Subscription.SubscriptionStatus.ACTIVE, endDateTime, startDateTime);

        double totalRevenue = 0;
        for (Subscription sub : activeSubscriptions) {
            totalRevenue += getPriceForSubscriptionLevel(sub.getSubscriptionLevel());
        }
        return totalRevenue;
    }

    // Taux de rétention (exemple simplifié - nécessite une logique plus complexe)
    public double getRetentionRate(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();

        long activeAtStart = subscriptionRepository
                .countByStatusAndStartDateLessThan(Subscription.SubscriptionStatus.ACTIVE, startDateTime);
        if (activeAtStart == 0) {
            return 0;
        }
        // Ceci est une simplification et ne prend pas en compte les annulations.
        long activeAtEnd = subscriptionRepository
                .countByStatusAndEndDateGreaterThanEqual(Subscription.SubscriptionStatus.ACTIVE, endDateTime);
        return (double) activeAtEnd / activeAtStart;
    }

    // Nombre d'abonnements avec le statut PAYMENT_FAILED
    public long getFailedPaymentCount() {
        return subscriptionRepository.countByStatus(Subscription.SubscriptionStatus.PAYMENT_FAILED);
    }

    // Méthode pour obtenir le prix en fonction du niveau d'abonnement
    private double getPriceForSubscriptionLevel(SubscriptionLevel level) {
        if (level == null) {
            return 4.99; // Prix par défaut
        }

        // Utiliser le prix directement du niveau d'abonnement si disponible
        if (level.getPrice() != 0) {
            return level.getPrice();
        }

        // Logique de secours basée sur le niveau d'abonnement
        if (level.getLevel() != null) {
            switch (level.getLevel()) {
                case PREMIUM:
                    return 9.99;
                case ULTRA:
                    return 19.99;
                case BASIC:
                default:
                    return 4.99;
            }
        }

        return 4.99; // Basic par défaut si le niveau n'est pas défini
    }
}