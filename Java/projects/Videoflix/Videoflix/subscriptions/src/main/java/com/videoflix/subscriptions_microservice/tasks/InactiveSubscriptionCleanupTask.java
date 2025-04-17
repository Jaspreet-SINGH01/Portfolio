package com.videoflix.subscriptions_microservice.tasks;

import com.videoflix.subscriptions_microservice.entities.Subscription;
import com.videoflix.subscriptions_microservice.repositories.SubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@Component
public class InactiveSubscriptionCleanupTask {

    private static final Logger logger = LoggerFactory.getLogger(InactiveSubscriptionCleanupTask.class);

    private final SubscriptionRepository subscriptionRepository;

    // Délai après lequel un abonnement annulé est considéré comme archivable
    @Value("${subscriptions.cleanup.cancelled-retention-period:P90D}")
    private Period cancelledRetentionPeriod;

    // Délai après lequel abonnement inactif est supprimé
    @Value("${subscriptions.cleanup.inactive-deletion-period:P365D}")
    private Period inactiveDeletionPeriod;

    public InactiveSubscriptionCleanupTask(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    // Planification de l'exécution de cette tâche tous les jours à 5h00 du matin
    @Scheduled(cron = "0 0 5 * * *")
    @Transactional
    public void cleanupInactiveSubscriptions() {
        LocalDate archiveThreshold = LocalDate.now().minus(cancelledRetentionPeriod);
        LocalDate deletionThreshold = LocalDate.now().minus(inactiveDeletionPeriod);

        archiveCancelledSubscriptions(archiveThreshold);
        deleteOldInactiveSubscriptions(deletionThreshold);
    }

    private void archiveCancelledSubscriptions(LocalDate archiveThreshold) {
        List<Subscription> cancelledSubscriptions = subscriptionRepository
                .findByStatusAndCancellationDateBefore(Subscription.SubscriptionStatus.CANCELLED, archiveThreshold);

        logger.info("Traitement de {} abonnements annulés pour l'archivage (avant le {}).",
                cancelledSubscriptions.size(), archiveThreshold);

        for (Subscription subscription : cancelledSubscriptions) {
            // Log l'archivage (au lieu de la suppression immédiate)
            logger.info("Archivage de l'abonnement {} de l'utilisateur {} (annulé le {}).",
                    subscription.getId(), subscription.getUser().getId(), subscription.getEndDate());
            // Mise à jour du statut pour indiquer que l'abonnement est archivé
            subscription.setStatus(Subscription.SubscriptionStatus.ARCHIVED);
            subscriptionRepository.save(subscription);
            logger.info("Abonnement {} marqué comme archivé.", subscription.getId());
        }
    }

    private void deleteOldInactiveSubscriptions(LocalDate deletionThreshold) {
        List<Subscription> oldInactiveSubscriptions = subscriptionRepository
                .findInactiveBefore(deletionThreshold);

        logger.warn("Traitement de {} abonnements inactifs pour la suppression (avant le {}).",
                oldInactiveSubscriptions.size(), deletionThreshold);

        for (Subscription subscription : oldInactiveSubscriptions) {
            logger.warn(
                    "Suppression de l'abonnement inactif {} de l'utilisateur {} (statut: {}, date de fin: {}, date d'annulation: {}).",
                    subscription.getId(), subscription.getUser().getId(), subscription.getStatus(),
                    subscription.getEndDate(), subscription.getEndDate());
            subscriptionRepository.delete(subscription);
            logger.info("Abonnement {} supprimé.", subscription.getId());
        }
    }
}