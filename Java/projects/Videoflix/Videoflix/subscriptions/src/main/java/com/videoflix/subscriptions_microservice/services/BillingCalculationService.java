package com.videoflix.subscriptions_microservice.services;

import com.videoflix.subscriptions_microservice.entities.Subscription;
import com.videoflix.subscriptions_microservice.entities.SubscriptionLevel;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class BillingCalculationService {

    public LocalDateTime calculateNextBillingDate(Subscription subscription) {
        LocalDateTime lastBillingDate = subscription.getNextBillingDate();
        if (lastBillingDate == null) {
            lastBillingDate = subscription.getStartDate();
        }

        SubscriptionLevel level = subscription.getSubscriptionLevel();
        if (level != null && level.getBillingFrequency() != null) {
            switch (level.getBillingFrequency()) {
                case MONTHLY:
                    return lastBillingDate.plus(1, ChronoUnit.MONTHS);
                case QUARTERLY:
                    return lastBillingDate.plus(3, ChronoUnit.MONTHS);
                case YEARLY:
                    return lastBillingDate.plus(1, ChronoUnit.YEARS);
                default:
                    return null;
            }
        } else {
            return null;
        }
    }

    public LocalDateTime calculateNextBillingDateAfterReactivation(Subscription subscription) {
        LocalDateTime cancelledAt = subscription.getCancelledAt();
        SubscriptionLevel level = subscription.getSubscriptionLevel();

        if (cancelledAt != null && level != null && level.getBillingFrequency() != null) {
            switch (level.getBillingFrequency()) {
                case MONTHLY:
                    return cancelledAt.plusMonths(1);
                case QUARTERLY:
                    return cancelledAt.plusMonths(3);
                case YEARLY:
                    return cancelledAt.plusYears(1);
                default:
                    // Si le type de facturation est inconnu, on applique un fallback
                    return LocalDateTime.now().plusMonths(1);
            }
        } else {
            // Fallback si données manquantes
            return LocalDateTime.now().plusMonths(1);
        }
    }

}