package com.videoflix.subscriptions_microservice.tasks;

import com.videoflix.subscriptions_microservice.entities.Subscription;
import com.videoflix.subscriptions_microservice.repositories.SubscriptionRepository;
import com.videoflix.subscriptions_microservice.services.DataSynchronizationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataSynchronizationTask {

    private static final Logger logger = LoggerFactory.getLogger(DataSynchronizationTask.class);

    private final SubscriptionRepository subscriptionRepository;
    private final DataSynchronizationService dataSynchronizationService;

    @Value("${data.synchronization.enabled:false}")
    private boolean synchronizationEnabled;

    @Value("${data.synchronization.batch-size:100}")
    private int batchSize;

    public DataSynchronizationTask(SubscriptionRepository subscriptionRepository,
            DataSynchronizationService dataSynchronizationService) {
        this.subscriptionRepository = subscriptionRepository;
        this.dataSynchronizationService = dataSynchronizationService;
    }

    // Planification de l'exécution de cette tâche tous les jours à 4h00 du matin
    @Scheduled(cron = "${data.synchronization.cron:0 0 4 * * *}")
    public void synchronizeSubscriptionData() {
        if (!synchronizationEnabled) {
            logger.info("La synchronisation des données d'abonnement est désactivée.");
            return;
        }

        logger.info("Début de la synchronisation des données d'abonnement...");

        LocalDateTime lastSyncTimestamp = dataSynchronizationService.getLastSuccessfulSyncTimestamp();
        List<Subscription> subscriptionsToSync;
        int page = 0;

        try {
            do {
                subscriptionsToSync = subscriptionRepository.findSubscriptionsUpdatedSince(lastSyncTimestamp,
                        page * batchSize, batchSize);
                if (!subscriptionsToSync.isEmpty()) {
                    logger.info("Synchronisation du lot {} de {} abonnements (mis à jour depuis {}).",
                            page + 1, subscriptionsToSync.size(), lastSyncTimestamp);
                    dataSynchronizationService.synchronizeSubscriptions(subscriptionsToSync);
                    page++;
                }
            } while (!subscriptionsToSync.isEmpty());

            dataSynchronizationService.updateLastSuccessfulSyncTimestamp(LocalDateTime.now());
            logger.info("Synchronisation des données d'abonnement terminée.");

        } catch (Exception e) {
            logger.error("Erreur lors de la synchronisation des données d'abonnement : {}", e.getMessage(), e);
            // Gérer l'erreur (log, potentiellement alerter)
        }
    }
}