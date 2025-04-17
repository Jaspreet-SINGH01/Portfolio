package com.videoflix.subscriptions_microservice.services;

import com.videoflix.subscriptions_microservice.entities.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DataSynchronizationService {

    private static final Logger logger = LoggerFactory.getLogger(DataSynchronizationService.class);

    private final RestTemplate restTemplate;

    @Value("${crm.synchronization.url}")
    private String crmSyncUrl;

    @Value("${analytics.synchronization.url}")
    private String analyticsSyncUrl;

    // Simuler un stockage pour la dernière synchronisation réussie (en production,
    // utiliser une base de données)
    private LocalDateTime lastSuccessfulSyncTimestamp = LocalDateTime.MIN;

    public DataSynchronizationService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public LocalDateTime getLastSuccessfulSyncTimestamp() {
        return lastSuccessfulSyncTimestamp;
    }

    public void updateLastSuccessfulSyncTimestamp(LocalDateTime timestamp) {
        this.lastSuccessfulSyncTimestamp = timestamp;
        logger.info("Horodatage de la dernière synchronisation mis à jour à : {}", timestamp);
        // En production, enregistrer ceci dans une base de données.
    }

    public void synchronizeSubscriptions(List<Subscription> subscriptions) {
        logger.info("Début de la synchronisation de {} abonnements avec les systèmes externes.", subscriptions.size());

        // Synchronisation avec le CRM
        if (crmSyncUrl != null && !crmSyncUrl.isEmpty()) {
            try {
                restTemplate.postForObject(crmSyncUrl, subscriptions, Void.class);
                logger.info("{} abonnements synchronisés avec succès avec le CRM à : {}", subscriptions.size(),
                        crmSyncUrl);
            } catch (Exception e) {
                logger.error("Erreur lors de la synchronisation avec le CRM à {} : {}", crmSyncUrl, e.getMessage(), e);
                // Gérer l'erreur (réessayer, enregistrer les échecs, etc.)
            }
        } else {
            logger.warn("L'URL de synchronisation CRM n'est pas configurée.");
        }

        // Synchronisation avec le système d'analytics
        if (analyticsSyncUrl != null && !analyticsSyncUrl.isEmpty()) {
            try {
                restTemplate.postForObject(analyticsSyncUrl, subscriptions, Void.class);
                logger.info("{} abonnements synchronisés avec succès avec le système d'analytics à : {}",
                        subscriptions.size(), analyticsSyncUrl);
            } catch (Exception e) {
                logger.error("Erreur lors de la synchronisation avec le système d'analytics à {} : {}",
                        analyticsSyncUrl, e.getMessage(), e);
                // Gérer l'erreur (réessayer, enregistrer les échecs, etc.)
            }
        } else {
            logger.warn("L'URL de synchronisation du système d'analytics n'est pas configurée.");
        }

        logger.info("Synchronisation des abonnements terminée.");
    }

    private String analyticsSubscriptionCancelledUrl;

    public void notifyAnalyticsSubscriptionCancelledOnStripe(String stripeSubscriptionId,
            LocalDateTime cancellationDate) {
        if (analyticsSubscriptionCancelledUrl != null && !analyticsSubscriptionCancelledUrl.isEmpty()) {
            java.util.Map<String, Object> payload = new java.util.HashMap<>();
            payload.put("stripeSubscriptionId", stripeSubscriptionId);
            payload.put("cancellationDate", cancellationDate.toString());

            try {
                restTemplate.postForObject(analyticsSubscriptionCancelledUrl, payload, Void.class);
                logger.info(
                        "Notification d'annulation d'abonnement Stripe (ID: {}) envoyée avec succès à l'analytics à : {}",
                        stripeSubscriptionId, analyticsSubscriptionCancelledUrl);
            } catch (Exception e) {
                logger.error(
                        "Erreur lors de l'envoi de la notification d'annulation d'abonnement Stripe (ID: {}) à l'analytics à {} : {}",
                        stripeSubscriptionId, analyticsSubscriptionCancelledUrl, e.getMessage(), e);
                // Gérer l'erreur (log, réessai si nécessaire)
            }
        } else {
            logger.warn(
                    "L'URL de notification d'annulation d'abonnement Stripe pour l'analytics n'est pas configurée.");
        }
    }
}